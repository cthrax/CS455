package cs455.scaling.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import cs455.scaling.shared.GlobalLogger;

public class ThreadSafeSelector {
    private final Selector selector;
    private final Object registerLock = new Object();
    private volatile int threadsWaiting = 0;
    private final boolean selectRunning = false;
    private final Object notifyLock = new Object();

    public ThreadSafeSelector(Selector selector) {
        this.selector = selector;
    }

    public int select() throws IOException {
        int i = selector.select();
        return i;
    }

    public void register(SocketChannel channel, int op, Object callback) throws ClosedChannelException {
        synchronized (registerLock) {
            try {
                threadsWaiting++;
                registerLock.wait();
            } catch (InterruptedException e) {
                return;
            }
            try {
                channel.register(selector, op, callback);
                threadsWaiting--;
            } catch (ClosedChannelException e) {
                GlobalLogger.severe(this, "Failed to register channel because it was closed.");
                threadsWaiting--;
                throw e;
            }
        }
    }

    public void flushPendingRegisters() {
        while (threadsWaiting > 0) {
            synchronized (registerLock) {
                registerLock.notify();
            }
        }
    }
}
