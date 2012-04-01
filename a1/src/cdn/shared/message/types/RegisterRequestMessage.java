package cdn.shared.message.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cdn.shared.GlobalLogger;
import cdn.shared.message.IMessage;

public class RegisterRequestMessage implements IMessage {

    private final RouterInfo node;
    public RegisterRequestMessage(byte[] bytes) {
        node = new RouterInfo(bytes);
    }

    public RegisterRequestMessage(RouterInfo node) {
        this.node = node;
    }

    @Override
    public MessageType getType() {
        return MessageType.REGISTER_REQUEST;
    }

    @Override
    public byte[] getWireFormat() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);
            s.writeInt(getType().ordinal());
            s.write(node.getWireFormat());
            s.flush();

        } catch (IOException e) {
            GlobalLogger.warning(this, "Failed to get wireformat");
        }

        return stream.toByteArray();
    }

    public RouterInfo getInfo() {
        return node;
    }

}
