package cdn.shared.message;

import java.nio.channels.SocketChannel;

import cdn.shared.message.IMessage.MessageType;

public interface IMessageRouter {
    void handleMessage(SocketChannel clientConnection, byte[] message, MessageType type);
}
