package cs455.scaling.server;

import java.util.Deque;
import java.util.LinkedList;

public class WorkerQueue {
    Deque<IWorker> queue = new LinkedList<IWorker>();
    private volatile boolean isRunning = true;

    public IWorker poll() throws InterruptedException {
        synchronized (this) {
            while (queue.size() <= 0 && isRunning) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    throw ex;
                }
            }

            if (isRunning) {
                if (queue.size() <= 0) {
                    return null;
                } else {
                    return queue.poll();
                }
            } else {
                return null;
            }
        }
    }

    public void push(IWorker worker) {
        synchronized (this) {
            queue.add(worker);
            notify();
        }
    }

    public void stopAllWorkers() {
        isRunning = false;
    }
}
