package cdn.shared;

public interface IWorker extends Runnable {
    /**
     * Stops listener so that thread can exit normally or be joined.
     */
    void cancel();

    /**
     * Determine if listener is still running.
     *
     * @return true if running, false otherwise.
     */
    boolean getRunState();
}
