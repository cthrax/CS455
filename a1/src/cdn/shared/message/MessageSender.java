package cdn.shared.message;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import cdn.shared.GlobalLogger;
import cdn.shared.IQueuedMessage;
import cdn.shared.IWorker;
import cdn.shared.QueueManager;
import cdn.shared.message.IMessage.MessageType;

public class MessageSender implements Runnable, IWorker {

    private final IMessageRouter manager;
    private final QueueManager<IQueuedMessage> messageQueue;
    private volatile boolean running = true;

    public MessageSender(QueueManager<IQueuedMessage> messageQueue, IMessageRouter manager) {
        this.manager = manager;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        while (running) {
            IQueuedMessage message = messageQueue.getNextItem(500);

            if (message != null) {
                handleMessage(message);
            } else {
                Thread.yield();
            }
        }
    }

    public void handleMessage(IQueuedMessage message) {
        try {
            SocketChannel client = message.getSocketChannel();
            MessageReader reader;
            if (message.getBytes() == null) {
                reader = MessageReader.fromSocketChannel(client);
            } else {
                reader = new MessageReader(message.getBytes());
            }

            MessageType type = reader.getType();
            if (type == MessageType.INVALID) {
                GlobalLogger.warning(this, "Invalid message received.");

            } else {
                byte[] buf = new byte[reader.getSize()];
                reader.readRemains(buf);
                manager.handleMessage(client, buf, type);

            }
        } catch (IOException e) {
            GlobalLogger.info(this, "No data to be read from message.");
        }

    }

    @Override
    public synchronized void cancel() {
        running = false;
    }

    @Override
    public boolean getRunState() {
        return running;
    }

}
