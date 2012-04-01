package cdn.shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class CommandListener implements IWorker {

    private volatile boolean running = true;
    private final ICommandRunner runner;

    public CommandListener(ICommandRunner runner) {
        this.runner = runner;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (running) {
                if (reader.ready()) {
                    String buf = reader.readLine();
                    if (buf != null) {
                        String command = buf.toLowerCase();
                        String[] list = command.split(" ");
                        String[] rest = new String[list.length - 1];
                        for (int i = 1; i < list.length; i++) {
                            rest[i - 1] = list[i];
                        }

                        runner.handleCommand(list[0], rest);
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

}
