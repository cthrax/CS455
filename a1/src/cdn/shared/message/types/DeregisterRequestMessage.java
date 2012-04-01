package cdn.shared.message.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cdn.shared.message.IMessage;

public class DeregisterRequestMessage implements IMessage {

    private final RouterInfo node;

    public DeregisterRequestMessage(RouterInfo node) {
        this.node = node;
    }

    public DeregisterRequestMessage(byte[] bytes) {
        node = new RouterInfo(bytes);
    }

    @Override
    public MessageType getType() {
        return MessageType.DEREGISTER_REQUEST;
    }

    @Override
    public byte[] getWireFormat() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);
            s.writeInt(getType().ordinal());
            s.write(node.getWireFormat());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return stream.toByteArray();
    }

    public RouterInfo getInfo() {
        return node;
    }
}
