package cdn.shared;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface IQueuedMessage {
    public SocketChannel getSocketChannel();

    public ByteBuffer getBytes();
}