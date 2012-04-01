package cdn.shared.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import cdn.shared.GlobalLogger;

public class MessageWriter {
    private final Socket sock;
    private final byte[] bytes;

    public MessageWriter(Socket sock, byte[] bytes) {
        this.sock = sock;
        this.bytes = bytes;
    }

    public void write() throws IOException {
        OutputStream s;
        if (sock.getChannel() != null) {
            s = new ByteArrayOutputStream();
        } else {
            s = sock.getOutputStream();
        }
        DataOutputStream stream = new DataOutputStream(s);
        GlobalLogger.debug(this, "Writing out " + bytes.length + " bytes");
        stream.writeInt(bytes.length);
        stream.write(bytes, 0, bytes.length);
        stream.flush();

        if (sock.getChannel() != null) {
            int totalBytesWritten = 0;
            byte[] bytes = ((ByteArrayOutputStream) s).toByteArray();
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            Selector selector = Selector.open();
            while (totalBytesWritten < bytes.length && totalBytesWritten >= 0) {
                if (sock.getChannel().isBlocking()) {
                    totalBytesWritten += sock.getChannel().write(buf);
                } else {
                    SelectionKey key = sock.getChannel().register(selector, SelectionKey.OP_WRITE);
                    selector.select(2000);
                    if (selector.selectedKeys().remove(key)) {
                        totalBytesWritten += sock.getChannel().write(buf);
                    }
                }
            }
            selector.close();
        }
    }
}
