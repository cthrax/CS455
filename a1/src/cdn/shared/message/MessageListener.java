package cdn.shared.message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import cdn.shared.GlobalLogger;
import cdn.shared.IQueuedMessage;
import cdn.shared.IWorker;
import cdn.shared.QueueManager;

public class MessageListener implements IWorker {
    private final int portNum;
    private volatile boolean running = true;
    private final QueueManager<IQueuedMessage> messageQueue;
    private static int INTERUPT_INT_MS = 500;
    private final Selector selector;

    public MessageListener(int portNum, QueueManager<IQueuedMessage> messageQueue, Selector selector) {
        this.portNum = portNum;
        this.messageQueue = messageQueue;
        this.selector = selector;
    }

    /*
     * (non-Javadoc)
     *
     * @see cdn.IListener#cancel()
     */
    @Override
    public synchronized void cancel() {
        running = false;
    }

    /*
     * (non-Javadoc)
     *
     * @see cdn.IListener#getRunState()
     */
    @Override
    public synchronized boolean getRunState() {
        return running;
    }

    @Override
    public synchronized void run() {
        SocketChannel listener;

        // Create the server socket.
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            InetAddress ia = InetAddress.getLocalHost();
            InetSocketAddress isa = new InetSocketAddress(ia, portNum);
            serverChannel.socket().bind(isa);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            try {
                while (getRunState()) {

                    GlobalLogger.debug2(this, "restart listening");
                    while (selector.select(INTERUPT_INT_MS) > 0 && getRunState()) {
                        boolean valueInUse = false;
                        while (valueInUse == true) {
                            try {
                                wait();
                            } catch (InterruptedException ex) {
                                if (!getRunState()) {
                                    break;
                                }
                            }
                        }
                        valueInUse = true;

                        if (getRunState()) {
                            Iterator<SelectionKey> itr = selector.selectedKeys().iterator();
                            while (itr.hasNext()) {
                                SelectionKey cur = itr.next();
                                if (cur.isAcceptable() && cur.channel() instanceof ServerSocketChannel) {
                                    listener = ((ServerSocketChannel) cur.channel()).accept();
                                    messageQueue.queueItem(new QueuedMessage(listener, null), INTERUPT_INT_MS, this.getClass().getName());
                                    selector.selectedKeys().remove(cur);
                                    serverChannel.register(selector, SelectionKey.OP_ACCEPT);

                                } else if (cur.isReadable() && cur.channel() instanceof SocketChannel) {
                                    try {
                                        readFromStream(cur);
                                    } catch (IOException ex) {
                                        // Let's hope it was nothing serious and keeping going in our loop.
                                    }
                                } else if (cur.isConnectable() && !cur.channel().isOpen()) {
                                    GlobalLogger.debug(this, "Connection closed.");
                                    cur.cancel();
                                } else {
                                    GlobalLogger.debug(this, "Woke up for something, not sure what.");
                                }
                            }
                        }
                        valueInUse = false;
                        notify();
                    }
                }
                // Once out of the loop we are no longer running.
                cancel();

                selector.close();
                serverChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
                GlobalLogger.severe(this, "Unable to bind to socket.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            GlobalLogger.severe(this, "Unable to create server Socket.");
        } catch (ClosedSelectorException ex) {

        }
    }

    /**
     * Read all the data from the stream, using intermediary selectors as needed.
     *
     * @param cur the current selectionKey.
     * @throws IOException
     */
    private void readFromStream(SelectionKey cur) throws IOException {
        SocketChannel router = (SocketChannel) cur.channel();
        int totalBytesRead = 0;
        int ret = 0;
        ByteBuffer buf = ByteBuffer.allocate(4);

        while (totalBytesRead < 4 && ret >= 0) {
            ret = router.read(buf);
            totalBytesRead += ret;
        }

        if (ret == -1) {
            selector.selectedKeys().remove(cur);
            GlobalLogger.warning(this, "Had to close socket, need to re-initiate.");
            router.close();

        } else {
            buf.position(0);
            int size = buf.getInt();
            ByteBuffer buf2 = ByteBuffer.allocate(size + 4);
            buf2.position(4);
            if (safelyRead(router, buf2, size)) {
                buf2.put(buf.array());
                GlobalLogger.debug(this, "Receieved message from router.");
                messageQueue.queueItem(new QueuedMessage(router, buf2), INTERUPT_INT_MS, this.getClass().getName());
                if (selector.selectedKeys().remove(cur)) {
                    router.register(selector, SelectionKey.OP_READ);
                }
            }
        }
    }

    private boolean safelyRead(SocketChannel sock, ByteBuffer buf, int size) {
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
                        System.out.println("Read " + totalBytesRead + " bytes.");
                    }
                }
            }

            if (ret < 0) {
                if (ret == -1) {
                    GlobalLogger.debug(this, "EOS!!!!");
                    sock.close();
                    return false;
                }
                GlobalLogger.debug(this, "Error reading bytes.");
            }
            s.close();

            buf.position(0);
        } catch (IOException ex) {
            ex.printStackTrace();
            GlobalLogger.severe(this, "IOException throw while safely reading.");
            return false;
        }
        return true;
    }
}
