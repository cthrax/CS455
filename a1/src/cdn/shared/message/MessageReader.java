package cdn.shared.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import cdn.shared.GlobalLogger;
import cdn.shared.message.IMessage.MessageType;

public class MessageReader {
    private final ByteBuffer bytes;
    private int size = -1;
    private MessageType type = null;

    public static MessageReader fromSocketChannel(SocketChannel channel) throws IOException {
        int totalBytesRead = 0;
        ByteBuffer buf = ByteBuffer.allocate(4);

        while (totalBytesRead < 4 && totalBytesRead >= 0) {
            totalBytesRead += channel.read(buf);
        }

        if (totalBytesRead == -1) {
            GlobalLogger.warning(MessageReader.class, "Had to close socket, need to re-initiate.");
            channel.close();

        } else {
            buf.position(0);
            int size = buf.getInt();
            ByteBuffer buf2 = ByteBuffer.allocate(size + 4);
            buf2.position(4);
            if (safelyRead(channel, buf2, size)) {
                buf2.put(buf.array());
                return new MessageReader(buf2);
            }
        }

        return null;
    }

    private static boolean safelyRead(SocketChannel sock, ByteBuffer buf, int size) {
        try {
            int totalBytesRead = 0;
            int ret = 0;
            Selector s = Selector.open();
            while (totalBytesRead < size && ret >= 0) {
                if (sock.isBlocking()) {
                    ret = sock.read(buf);
                    totalBytesRead += ret;
                } else {
                    SelectionKey key = sock.register(s, SelectionKey.OP_READ);
                    s.select(2000);
                    if (s.selectedKeys().remove(key)) {
                        totalBytesRead += sock.read(buf);
                    }
                }
            }

            if (ret < 0) {
                if (ret == -1) {
                    GlobalLogger.debug(MessageReader.class, "EOS!!!!");
                    sock.close();
                    return false;
                }
                GlobalLogger.debug(MessageReader.class, "Error reading bytes.");
            }
            s.close();

            buf.position(0);
        } catch (IOException ex) {
            ex.printStackTrace();
            GlobalLogger.severe(MessageReader.class, "IOException throw while safely reading.");
            return false;
        }
        return true;
    }

    public MessageReader(ByteBuffer bytes) {
        this.bytes = bytes;
    }

    public MessageType getType() throws IOException {
        if (type == null) {
            bytes.position(4);
            type = MessageType.fromOrdinal(bytes.getInt());
        }
        return type;
    }

    public int getSize() throws IOException {
        if (size < 0) {
            bytes.position(0);
            size = bytes.getInt() - Integer.SIZE / 8;
        }
        return size;
    }

    public void readRemains(byte[] bytes) throws IOException {
        this.bytes.position(8);

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = this.bytes.get();
        }
    }
}
