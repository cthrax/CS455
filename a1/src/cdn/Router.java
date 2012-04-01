package cdn;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.Selector;
import java.util.LinkedList;
import java.util.List;

import cdn.router.RouterArgumentParser;
import cdn.router.RouterCommunicator;
import cdn.router.RoutingPlan;
import cdn.shared.CommandListener;
import cdn.shared.GlobalLogger;
import cdn.shared.ICommandExecutor;
import cdn.shared.ICommandRunner;
import cdn.shared.IQueuedMessage;
import cdn.shared.IWorker;
import cdn.shared.MessageException;
import cdn.shared.MessagePrinter;
import cdn.shared.QueueManager;
import cdn.shared.message.MessageListener;
import cdn.shared.message.MessageSender;
import cdn.shared.message.types.RouterInfo;

public class Router implements ICommandRunner, UncaughtExceptionHandler {
    QueueManager<ICommandExecutor> commands = new QueueManager<ICommandExecutor>();
    QueueManager<IQueuedMessage> messageQueue = new QueueManager<IQueuedMessage>();
    List<IWorker> workers = new LinkedList<IWorker>();
    List<Thread> threads = new LinkedList<Thread>();
    private final int portNum;
    private RouterCommunicator routerCommunicator = null;
    private Selector selector;
    private boolean running = true;
    private final String id;
    private final RouterInfo self;
    private boolean connected = true;

    public Router(String id, int portNum, InetAddress discoveryNodeAddr, int discoveryNodePort) throws UnknownHostException, IOException, MessageException {
        this.portNum = portNum;
        this.id = id;
        try {
            selector = Selector.open();
            String host = InetAddress.getLocalHost().getHostAddress();
            GlobalLogger.info(this, "Starting router at " + host + ":" + portNum);
            self = new RouterInfo(id, host, portNum);
            routerCommunicator = new RouterCommunicator(discoveryNodeAddr, discoveryNodePort, self, selector);
        } catch (UnknownHostException e) {
            GlobalLogger.severe(this, "Unable to lookup localhost, shutting down.");
            throw e;
        } catch (IOException e) {
            GlobalLogger.severe(this, "Unable to create selector for listening");
            throw e;
        } catch (MessageException ex) {
            throw ex;
        }
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void run() {
        startMessageListener();
        startMessageSender();
        startCommandListener();

        while (running) {
            ICommandExecutor cmd = commands.getNextItem(500);

            if (cmd != null) {
                cmd.execute();
            }
        }
    }

    public void shutdown() {
        try {
            selector.close();
        } catch (IOException ex) {
            // Don't care.
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

        running = false;
    }

    /**
     * Adds valid commands to the command queue to be executed.
     * Some pre-conditions are that the command is already lower-case and that
     * arguments are split into the array.
     */
    @Override
    public synchronized void handleCommand(String command, String[] args) {
        RouterCommandType cmd = RouterCommandType.fromString(command);

        switch (cmd) {
            case PRINT_MST:
                if (connected) {
                    RoutingPlan plan = routerCommunicator.getRouterPlan();
                    System.out.println(MessagePrinter.print(plan, self));
                } else {
                    System.out.println("The best and most efficient route to myself, is to do nothing. So I did. (No longer connected to CDN).");
                }
                break;
            case SEND_DATA:
                if (connected) {
                    routerCommunicator.sendData();
                } else {
                    System.out.println("Talking to one's self is a sign of mental instability. So I'll refrain. (No longer connected to CDN).");
                }
                break;
            case EXIT_CDN:
                if (connected) {
                    try {
                        System.out.println("Exiting CDN...");
                        routerCommunicator.exitCdn();
                        connected = false;
                        System.out.println("There is no method of re-registration, so most commands (" + RouterCommandType.SEND_DATA.getForm() + " and "
                                + RouterCommandType.PRINT_MST.getForm() + ") will do nothing or error. I recommend you type 'exit'.");
                    } catch (MessageException ex) {
                        GlobalLogger.severe(this, "Failed to deregister, please retry or type 'exit' to force shutdown.");
                    }
                } else {
                    System.out.println("In order to exit, one must first enter. (Not connected to CDN.)");
                }
                break;
            case EXIT:
            case QUIT:
                shutdown();
                break;
            default:
                System.out.println("Unknown command.");
                System.out.print("Try one of ");
                for (RouterCommandType t : RouterCommandType.values()) {
                    if (t != RouterCommandType.INVALID) {
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
        createThread(new MessageSender(messageQueue, routerCommunicator), "MessageSender");
    }

    private void createThread(IWorker r, String name) {
        Thread newThread = new Thread(r, name);
        newThread.start();
        workers.add(r);
        threads.add(newThread);
    }

    @Override
    public void uncaughtException(Thread arg0, Throwable arg1) {
        arg1.printStackTrace();
        GlobalLogger.severe(this, "Received uncaught exception (" + arg1.toString() + ") from Thread: " + arg0.getName() + " with message:" + arg1.getMessage());
    }

    private enum RouterCommandType {
        PRINT_MST("print-mst"),
        SEND_DATA("send-data"),
        EXIT_CDN("exit-cdn"),
        EXIT("exit"),
        QUIT("quit"),
        INVALID("");

        private String form;

        RouterCommandType(String form) {
            this.form = form;
        }

        public String getForm() {
            return form;
        }

        public static RouterCommandType fromString(String value) {
            for (int i = 0; i < RouterCommandType.values().length; i++) {
                if (RouterCommandType.values()[i].getForm().equals(value)) {
                    return RouterCommandType.values()[i];
                }
            }

            return INVALID;
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        RouterArgumentParser parser = new RouterArgumentParser(args);

        if (parser.isValid()) {
            try {
                Router router = new Router(parser.getId(), parser.getPortNum(), parser.getDiscoveryHost(), parser.getDiscoveryPort());
                router.run();
            } catch (UnknownHostException e) {
                return;
            } catch (IOException e) {
                return;
            } catch (MessageException ex) {
                return;
            }
        }
    }

}
