package cs455.scaling.server;

public class WorkerThread extends Thread implements ICanSignalStop {
    private final WorkerQueue queue;
    private volatile boolean isRunning = true;

    public WorkerThread(WorkerQueue queue, String name) {
        super(name);
        this.queue = queue;
    }

    @Override
    public void signalStop() {
        isRunning = false;
        interrupt();
    }

    @Override
    public void run() {
        while (isRunning && !interrupted()) {
            try {
                IWorker worker = queue.poll();
                if (worker != null && isRunning) {
                    worker.execute();
                }
            } catch (InterruptedException ex) {

            }
        }
    }

}
