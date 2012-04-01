package cdn.shared.message;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import cdn.shared.IQueuedMessage;

public class QueuedMessage implements IQueuedMessage {

    private final SocketChannel client;
    private final ByteBuffer bytes;

    public QueuedMessage(SocketChannel client, ByteBuffer bytes) {
        this.client = client;
        this.bytes = bytes;
    }

    @Override
    public SocketChannel getSocketChannel() {
        return client;
    }

    public ByteBuffer getBytes() {
        return bytes;
    }

}
