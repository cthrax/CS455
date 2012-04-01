package cdn.shared.message;

import static org.testng.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.Selector;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cdn.discovery.RouterManager;
import cdn.shared.GlobalLogger;
import cdn.shared.IQueuedMessage;
import cdn.shared.IWorker;
import cdn.shared.QueueManager;
import cdn.shared.message.IMessage.MessageType;
import cdn.shared.message.types.RegisterRequestMessage;
import cdn.shared.message.types.RouterInfo;

public class TestMessageSendReceive {
    QueueManager<IQueuedMessage> messageQueue;
    IWorker receiver;
    IWorker sender;

    Thread senderT;
    Thread receiverT;

    Selector selector = null;

    @BeforeMethod
    public void init() {
        try {
            selector = Selector.open();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        messageQueue = new QueueManager<IQueuedMessage>();
        receiver = new MessageListener(8000, messageQueue, selector);
        RouterManager manager = new RouterManager();
        sender = new MessageSender(messageQueue, manager);

        senderT = new Thread(sender);
        senderT.start();

        receiverT = new Thread(receiver);
        receiverT.start();
    }

    @Test
    public void testSendMessage() {
        RegisterRequestMessage message = new RegisterRequestMessage(new RouterInfo("A", "1.1.1.0", 80));
        try {
            // Give some time for everything to get setup.
            Thread.sleep(500);
            Socket client = new Socket(InetAddress.getLocalHost(), 8000);
            new MessageWriter(client, message.getWireFormat()).write();

            DataInputStream response = new DataInputStream(client.getInputStream());
            GlobalLogger.debug2(this, "reading an int.");
            response.readInt();
            GlobalLogger.debug2(this, "int read.");
            assertEquals(response.readInt(), MessageType.REGISTER_RESPONSE.ordinal());
            GlobalLogger.debug2(this, "cancelling.");
            sender.cancel();
            receiver.cancel();
            GlobalLogger.debug2(this, "cancelled...joining.");

            if (senderT.isAlive()) {
                senderT.join();
            }

            if (receiverT.isAlive()) {
                receiverT.join();
            }
            GlobalLogger.debug2(this, "joined.");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
