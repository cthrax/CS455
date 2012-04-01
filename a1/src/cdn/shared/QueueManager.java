package cdn.shared;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class QueueManager<E> {
    BlockingQueue<E> itemQueue = new LinkedBlockingQueue<E>();

    /**
     * Class used to safely add an item to a queue from multiple threads.
     *
     * @param item the item to add to the queue.
     * @param timeout the amount of time to wait if the queue is currently inaccessible.
     */
    public synchronized void queueItem(E item, int timeout) {
        queueItem(item, timeout, "unspecified");
    }

    /**
     * Class used to safely add an item to a queue from multiple threads.
     *
     * @param item the item to add to the queue.
     * @param timeout the amount of time to wait if the queue is currently inaccessible.
     * @param name the name to display in the log.
     */
    public synchronized void queueItem(E item, int timeout, String name) {
        try {
            if (!itemQueue.offer(item, timeout, TimeUnit.MILLISECONDS)) {
                GlobalLogger.warning(this, "Unable to add a " + name + " command to queue before next refresh.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            GlobalLogger.warning(this, "Adding a " + name + " command was interrupted.");
        }
    }

    public E getNextItem(int timeout) {
        try {
            return itemQueue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // Not that dangerous because this will be called from the main thread which is looping for the next command all the time.
            GlobalLogger.info(this, "Interrupted while retrieving next item.");
            e.printStackTrace();
            return null;
        }
    }
}
