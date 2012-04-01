package cdn.shared.message.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cdn.shared.message.IMessage;

public class RouterConnectionMessage implements IMessage {

    private final RouterInfo router;

    public RouterConnectionMessage(RouterInfo router) {
        this.router = router;
    }

    public RouterConnectionMessage(byte[] bytes) {
        router = new RouterInfo(bytes);
    }

    @Override
    public byte[] getWireFormat() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream s = new DataOutputStream(stream);
        try {
            s.writeInt(getType().ordinal());
            s.write(router.getWireFormat());
            s.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream.toByteArray();
    }

    public RouterInfo getRouter() {
        return router;
    }

    @Override
    public MessageType getType() {
        return MessageType.ROUTER_CONNECTION;
    }

}
