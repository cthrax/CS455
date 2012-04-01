package cdn;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.Selector;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cdn.discovery.DiscoveryArgumentParser;
import cdn.discovery.RouterManager;
import cdn.shared.CommandListener;
import cdn.shared.GlobalLogger;
import cdn.shared.ICommandExecutor;
import cdn.shared.ICommandRunner;
import cdn.shared.IQueuedMessage;
import cdn.shared.IWorker;
import cdn.shared.MessagePrinter;
import cdn.shared.QueueManager;
import cdn.shared.message.MessageListener;
import cdn.shared.message.MessageSender;

public class Discovery implements ICommandRunner, UncaughtExceptionHandler {
    QueueManager<ICommandExecutor> commands = new QueueManager<ICommandExecutor>();
    QueueManager<IQueuedMessage> messageQueue = new QueueManager<IQueuedMessage>();
    List<IWorker> workers = new LinkedList<IWorker>();
    List<Thread> threads = new LinkedList<Thread>();
    private final int portNum;
    private int refreshInterval = 120; // seconds
    private final Timer refreshTimer;
    private final RouterManager routerManager;
    private Selector selector;
    private boolean running = true;

    public Discovery(int portNum, int refreshInterval) throws IOException {
        this.portNum = portNum;
        this.refreshInterval = refreshInterval;
        refreshTimer = new Timer();
        routerManager = new RouterManager();

        try {
            selector = Selector.open();
        } catch (IOException ex) {
            GlobalLogger.severe(this, "Unable to open selector for server.");
            throw ex;
        }
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void run() {
        try {
            System.out.println("Starting up discovery at " + InetAddress.getLocalHost().getHostAddress() + ":" + portNum);
        } catch (UnknownHostException ex) {
            GlobalLogger.severe(this, "Failed to get localhost address.");
        }
        startMessageListener();
        startMessageSender();
        startCommandListener();
        startRefreshTimer();

        // TODO: put in logic to stop gracefully.
        while (running) {
            ICommandExecutor cmd = commands.getNextItem(500);

            if (cmd != null) {
                cmd.execute();
            }
        }
    }

    /**
     * Adds valid commands to the command queue to be executed.
     * Some pre-conditions are that the command is already lower-case and that
     * arguments are split into the array.
     */
    @Override
    public synchronized void handleCommand(String command, String[] args) {
        DiscoveryCommand cmd = DiscoveryCommand.fromString(command);

        switch(cmd) {
            case SETUP_CDN:
                System.out.println("Setting up cdn...");
                if (args.length > 0) {
                    try {
                        int num = Integer.parseInt(args[0]);
                        if (num >= routerManager.getRouters().length) {
                            System.out.println("To ensure only one per each router, Cr must be less than number of nodes.");
                        } else if (num == 1 && routerManager.getRouters().length != 2) {
                            System.out.println("More connections are needed to create a network of this size.");
                        } else if (num <= 0 || routerManager.getRouters().length <= 1) {
                            System.out.println("Invalid parameters for a network.");
                        } else {
                            commands.queueItem(new SetupCdnCommand(routerManager, num), 500);
                            commands.queueItem(new RefreshWeights(routerManager), 500);
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println(DiscoveryCommand.SETUP_CDN.getForm() + " requires a numeric argument.");
                    }
                } else {
                    System.out.println(DiscoveryCommand.SETUP_CDN.getForm() + " requires an argument.");
                }
                break;
            case LIST_ROUTERS:
                System.out.println("Registered routers:");
                System.out.println(MessagePrinter.print(routerManager.getRouters()));
                break;
            case LIST_WEIGHTS:
                if (routerManager.isSetup()) {
                    System.out.println(MessagePrinter.print(routerManager.getEdges()));
                } else {
                    System.out.println("No CDN presently setup, try running " + DiscoveryCommand.SETUP_CDN.getForm() + " first.");
                }
                break;
            case QUIT:
            case EXIT:
                shutdown();
                break;
            default:
                System.out.println("Unknown command.");
                System.out.print("Try one of ");
                for (DiscoveryCommand t : DiscoveryCommand.values()) {
                    if (t != DiscoveryCommand.INVALID) {
                        System.out.print(t.getForm() + " ");
                    }
                }
                System.out.println("");
                break;
        }
    }

    private void startMessageListener() {
        createThread(new MessageListener(portNum, messageQueue, selector), "MessageListener");
    }

    private void startCommandListener() {
        createThread(new CommandListener(this), "CommandListener");
    }

    private void startMessageSender() {
        createThread(new MessageSender(messageQueue, routerManager), "MessageSender");
    }

    private void createThread(IWorker r, String name) {
        Thread newThread = new Thread(r, name);
        newThread.start();
        workers.add(r);
        threads.add(newThread);
    }

    public void shutdown() {
        try {
            selector.close();
        } catch (IOException ex) {

        }

        for (IWorker worker : workers) {
            worker.cancel();
        }

        for (Thread thread : threads) {
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                GlobalLogger.severe(this, "Failed to join thread " + thread.getName() + " trying to join next.");
            }
        }
        refreshTimer.cancel();
        running = false;
    }

    private void startRefreshTimer() {
        // Schedule a refresh of the edge weights at the passed in interval.
        refreshTimer.scheduleAtFixedRate(new RefreshWeightsTask(), refreshInterval * 1000, refreshInterval * 1000);
    }

    @Override
    public void uncaughtException(Thread arg0, Throwable arg1) {
        GlobalLogger.severe(this, "Received uncaught exception (" + arg1.toString() + ") from Thread: " + arg0.getName() + " with message:" + arg1.getMessage());
    }

    private class RefreshWeightsTask extends TimerTask {
        @Override
        public void run() {
            GlobalLogger.debug(this, "Ran refresh timer.");
            commands.queueItem(new RefreshWeights(routerManager), refreshInterval * 1000 - 100);
        }

    }

    private static class RefreshWeights implements ICommandExecutor {
        private final RouterManager routerManager;

        public RefreshWeights(RouterManager routerManager) {
            this.routerManager = routerManager;
        }

        @Override
        public void execute() {
            routerManager.advertiseLinkWeights();
        }

    }

    private class SetupCdnCommand implements ICommandExecutor {
        private final int numberOfPeers;
        private final RouterManager routerManager;

        public SetupCdnCommand(RouterManager routerManager, int numberOfPeers) {
            this.numberOfPeers = numberOfPeers;
            this.routerManager = routerManager;
        }

        @Override
        public void execute() {
            routerManager.advertisePeerList(numberOfPeers);
        }

    }

    private static enum DiscoveryCommand {
        LIST_ROUTERS("list-routers"),
        LIST_WEIGHTS("list-weights"),
        SETUP_CDN("setup-cdn"),
        EXIT("exit"),
        QUIT("quit"),
        INVALID("");

        private final String form;

        private DiscoveryCommand(String form) {
            this.form = form;
        }

        public String getForm() {
            return form;
        }

        public static DiscoveryCommand fromString(String cmd) {
            for (int i = 0; i < DiscoveryCommand.values().length; i++) {
                if (DiscoveryCommand.values()[i].getForm().equals(cmd)) {
                    return DiscoveryCommand.values()[i];
                }
            }

            return INVALID;
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        DiscoveryArgumentParser parser = new DiscoveryArgumentParser(args);

        if (parser.isValid()) {
            Discovery main;
            try {
                main = new Discovery(parser.getPortNum(), parser.getRefreshInterval());
                main.run();
            } catch (IOException e) {
                return;
            }
        }
    }
}
