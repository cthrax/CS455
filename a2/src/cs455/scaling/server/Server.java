package cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import cs455.scaling.server.workers.ReadWorker;
import cs455.scaling.shared.GlobalLogger;

public class Server {
    private final int port;
    private final ThreadPool threadPool;
    private volatile boolean isRunning = true;

    public static int servedCounter = 0;

    public synchronized static void incrementServed() {
        servedCounter++;
    }

    public Server(int port, int threadCount) {
        this.port = port;
        threadPool = new ThreadPool(threadCount, new WorkerQueue());
    }

    public void run() {
        try {
            GlobalLogger.info(this, "Starting server on " + InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ex) {
            GlobalLogger.info(this, "Starting server... ");
        }

        threadPool.start();
        GlobalLogger.info(this, "ThreadPool size: " + threadPool.getNumberOfWorkers());
        GlobalLogger.info(this, "");

        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost(), port);
            serverChannel.socket().bind(isa);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            ThreadSafeSelector tselector = new ThreadSafeSelector(selector);
            ServerStatus status = new ServerStatus();
            int count = 0;

            while (isRunning) {
                int selected = selector.select(1);
                if (selected > 0 && isRunning) {
                    Iterator<SelectionKey> itr = selector.selectedKeys().iterator();
                    while (itr.hasNext() && !Thread.interrupted()) {
                        SelectionKey cur = itr.next();
                        itr.remove();
                        // Disable interest in these ops
                        cur.interestOps(cur.interestOps() & ~cur.readyOps());
                        if (cur.isAcceptable() && cur.channel() instanceof ServerSocketChannel) {
                            SocketChannel client = ((ServerSocketChannel) cur.channel()).accept();
                            client.configureBlocking(false);
                            status.addClient();
                            // Add new socket to selector
                            client.register(selector, SelectionKey.OP_READ);

                            // Remove from selectedKeys so we can move to next
                            selector.selectedKeys().remove(cur);

                            // Re-register with selector so we can receive more connections
                            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                        } else if (cur.isReadable() && cur.channel() instanceof SocketChannel && (cur.attachment() == null || cur.attachment() instanceof ReadWorker)) {
                            SocketChannel channel = (SocketChannel) cur.channel();
                            Object reader = cur.attachment();
                            cur.cancel();

                            // If this is a continuation or an initial read.
                            if (reader == null) {
                                threadPool.addWorker(new ReadWorker(tselector, channel, status));
                            } else {
                                threadPool.addWorker((ReadWorker) reader);
                            }

                        } else if (!cur.isValid()) {
                            if (cur.channel() != null && !((SocketChannel) cur.channel()).isOpen()) {
                                status.removeClient();
                            }
                        }
                    }
                }

                // Print out server stats every 30 seconds or so.
                if (count++ == 5000) {
                    count = 0;

                    GlobalLogger.info(this, "===================");
                    GlobalLogger.info(this, "Status Report:");
                    GlobalLogger.info(this, "===================");
                    GlobalLogger.info(this, "Clients connected: " + status.getClientCount());
                    GlobalLogger.info(this, "Packets per second: " + status.getPacketsPerSecond());
                    GlobalLogger.info(this, "Uptime: " + status.getUptime().getFormatted());
                    GlobalLogger.info(this, "");
                }

                if (selected <= 0) {
                    tselector.flushPendingRegisters();
                }
            }

            selector.close();
            threadPool.shutdown();
        } catch (IOException ex) {
            GlobalLogger.severe(this, "IO Exception: " + ex.getMessage());
        }
    }

    public void stop() {
        isRunning = false;
        Thread.currentThread().interrupt();
    }

    public static void main(String[] args) {

        ServerArgumentParser parser = new ServerArgumentParser(args);

        if (parser.isValid()) {
            Server s = new Server(parser.getPortNum(), parser.getThreadPoolSize());
            Runtime.getRuntime().addShutdownHook(new ShutdownThread(s));
            s.run();
        }
    }

    public static class ShutdownThread extends Thread {
        private final Server server;

        public ShutdownThread(Server server) {
            this.server = server;
        }

        @Override
        public void run() {
            server.stop();
        }
    }
}
