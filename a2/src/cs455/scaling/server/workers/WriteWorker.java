package cs455.scaling.server.workers;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cs455.scaling.server.IWorker;
import cs455.scaling.server.ServerStatus;
import cs455.scaling.shared.GlobalLogger;

public class WriteWorker implements IWorker {

    private final ByteBuffer buf;
    private int totalBytesWritten = 0;
    private final int errorCount = 0;
    protected final SocketChannel sockc;
    private final ServerStatus status;

    public WriteWorker(SocketChannel sockc, ByteBuffer buf, ServerStatus status) {
        this.buf = getHash(buf);
        this.sockc = sockc;
        this.status = status;
    }

    private ByteBuffer getHash(ByteBuffer data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");

            byte[] hash = digest.digest(data.array());
            return ByteBuffer.wrap(hash);
        } catch (NoSuchAlgorithmException e) {
            GlobalLogger.severe(this, "No SHA1 algorithm, wtf?");
        }

        return null;
    }

    @Override
    public void execute() {
        try {
            Selector selector = Selector.open();
            SelectionKey key = sockc.register(selector, SelectionKey.OP_WRITE);
            String clientName = ((SocketChannel) key.channel()).socket().getInetAddress().getHostName();
            while (totalBytesWritten < buf.array().length && key.isValid()) {
                try {
                    int selected = selector.select(1);
                    if (key.isWritable() && selected > 0) {
                        writeToStream(selector);
                    }
                } catch (IOException ex) {
                    GlobalLogger.severe(this, "Failed to select on write selector.");
                }
            }

            System.out.println(String.format("[ClientMessage - %s] - %s", clientName, new BigInteger(1, buf.array()).toString(16)));
            System.out.println(String.format("[ServerMessage] Served by thread: %s", Thread.currentThread().getName()));
            status.packetServed();
            selector.close();
            GlobalLogger.debug2(this, "Done writing.");
        } catch (IOException ex) {
            GlobalLogger.severe(this, "Failed to register write with write selector.");
        }
    }

    private void writeToStream(Selector selector) {
        int ret = 0;
        boolean nothingToWrite = buf == null;

        if (!nothingToWrite) {
            try {
                ret = sockc.write(buf);
            } catch (IOException e) {
                GlobalLogger.severe(this, "IOException received while writing: " + e.getMessage());
                try {
                    status.removeClient();
                    sockc.close();
                    return;
                } catch (IOException ex) {
                    // Don't care.
                }
            }
        } else {
            GlobalLogger.info(this, "Writer didn't have anything to write, this probably means a client hasn't been responded to.");
        }

        if (ret > 0) {
            totalBytesWritten += ret;
        }

        if (nothingToWrite || ret > 0 && totalBytesWritten >= buf.array().length) {
            GlobalLogger.debug2(this, "Successfully wrote all bytes.");

        } else if (ret == -1) {
            GlobalLogger.severe(this, "End of stream received, had to close connection.");
            try {
                status.removeClient();
                sockc.close();
            } catch (IOException e) {
                // Ignore a failed close.
            }

        } else { // Probably wasn't writeable, put back in loop
            try {
                sockc.register(selector, SelectionKey.OP_WRITE);
            } catch (ClosedChannelException e) {
                status.removeClient();
                GlobalLogger.severe(this, "Failed to a write socket because the socket was closed.");
            }

        }
    }

}
