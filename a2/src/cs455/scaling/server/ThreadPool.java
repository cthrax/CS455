package cs455.scaling.server;

import java.util.ArrayList;
import java.util.List;

import cs455.scaling.shared.GlobalLogger;

public class ThreadPool {
    private final WorkerQueue queue;
    private final List<WorkerThread> threads;

    public ThreadPool(int workerCount, WorkerQueue queue) {
        this.queue = queue;
        threads = new ArrayList<WorkerThread>(workerCount);

        for (int i = 0; i < workerCount; i++) {
            threads.add(new WorkerThread(queue, "worker-" + i));
        }
    }

    public int getNumberOfWorkers() {
        return threads.size();
    }

    public void addWorker(IWorker worker) {
        queue.push(worker);
    }

    public void start() {
        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).start();
        }
    }

    public void shutdown() {
        queue.stopAllWorkers();
        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).signalStop();
        }

        for (int i = 0; i < threads.size(); i++) {
            try {
                threads.get(i).join(50);
            } catch (InterruptedException e) {
                GlobalLogger.warning(this, "Failed to join thread, attempting interrupt and retrying.");
            }
        }
    }
}
