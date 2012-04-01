package cs455.scaling.server.workers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import cs455.scaling.server.IWorker;
import cs455.scaling.server.ServerStatus;
import cs455.scaling.server.ThreadSafeSelector;
import cs455.scaling.shared.GlobalLogger;

public class ReadWorker implements IWorker {

    private static final int PACKET_SIZE = 8192;
    private ByteBuffer buf = null;
    private int totalBytesRead = 0;
    protected final ThreadSafeSelector mainSelector;
    protected final SocketChannel sockc;
    private final ServerStatus status;

    public ReadWorker(ThreadSafeSelector mainSelector, SocketChannel sockc, ServerStatus status) {
        this.mainSelector = mainSelector;
        this.sockc = sockc;
        this.status = status;
    }

    @Override
    public void execute() {
        if (buf == null) {
            buf = ByteBuffer.allocate(PACKET_SIZE);
        }

        GlobalLogger.debug2(this, "Reading from stream.");
        readFromStream();
    }

    /**
     * Read all the data from the stream, using intermediary selectors as needed.
     *
     * @param cur the current selectionKey.
     */
    private void readFromStream() {
        int ret = 0;
        if (totalBytesRead < PACKET_SIZE) {
            try {
                ret = sockc.read(buf);
            } catch (IOException e) {
                // XXX: Should this re-register for reading or close? At this point assume re-register.
                GlobalLogger.severe(this, "Failed to read from stream with IOException: " + e.getMessage());
                return;
            }

            if (ret > 0) {
                totalBytesRead += ret;
            }
        }

        if (ret == -1) {
            GlobalLogger.warning(this, "Had to close socket.");
            try {
                status.removeClient();
                sockc.close();
            } catch (IOException e) {
                // Ignore failed close.
            }

        } else if (ret <= 0) {
            GlobalLogger.warning(this, "Got an error from read: " + ret);
            try {
                status.removeClient();
                sockc.close();
            } catch (IOException e) {
                // Ignore failed close.
            }
        } else if (totalBytesRead >= PACKET_SIZE) {
            new WriteWorker(sockc, buf, status).execute();
            try {
                mainSelector.register(sockc, SelectionKey.OP_READ, null);
            } catch (IOException ex) {
                GlobalLogger.debug(this, "Failed to re-register read socket after write.");
            }

        } else {
            try {
                mainSelector.register(sockc, SelectionKey.OP_READ, this);
            } catch (ClosedChannelException e) {
                status.removeClient();
                GlobalLogger.severe(this, "Can't re-register a read connection because the channel is closed. (2)");
            }
        }
    }

}
