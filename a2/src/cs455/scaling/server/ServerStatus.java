package cs455.scaling.server;

import java.util.concurrent.atomic.AtomicLong;

public class ServerStatus {
    private final AtomicLong clientsConnected = new AtomicLong();
    private final AtomicLong packetsServed = new AtomicLong();
    private final AtomicLong lastPacketCount = new AtomicLong();
    private final AtomicLong lastStartTime = new AtomicLong();
    private final long startTime;

    public ServerStatus() {
        lastStartTime.set(System.currentTimeMillis());
        lastPacketCount.set(0);
        startTime = System.currentTimeMillis();
    }

    public void addClient() {
        clientsConnected.incrementAndGet();
    }

    public void removeClient() {
        clientsConnected.decrementAndGet();
    }

    public void packetServed() {
        packetsServed.incrementAndGet();
    }

    public long getClientCount() {
        return clientsConnected.get();
    }

    public int getPacketsPerSecond() {
        // This is not entirely thread-safe, however, the worst that could happen is a packet or two gets added,
        // between grabbing the current start time and getting the packet count, since this is an average anyway
        // a packet or two should not make a difference, certainly not enough to accrue the overhead of a synchronize
        // block.
        long start = lastStartTime.getAndSet(System.currentTimeMillis());
        return (int) (packetsServed.getAndSet(0) / ((System.currentTimeMillis() - start) / 1000));
    }

    public TimeSpan getUptime() {
        return new TimeSpan(startTime, System.currentTimeMillis());
    }
}
