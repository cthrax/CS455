package cdn.shared.message.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cdn.shared.message.IMessage;

public class PeerRouterListMessage implements IMessage {

    private RouterInfo[] routers;

    public PeerRouterListMessage(RouterInfo[] routers) {
        this.routers = routers;
    }

    public PeerRouterListMessage(byte[] bytes) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        try {
            DataInputStream s = new DataInputStream(stream);
            int numberOfRouters = s.readInt();
            routers = new RouterInfo[numberOfRouters];

            for (int i = 0; i < numberOfRouters; i++) {
                int routerSize = s.readInt();
                byte[] reader = new byte[routerSize];
                s.read(reader, 0, routerSize);
                routers[i] = new RouterInfo(reader);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public RouterInfo[] getRouters() {
        return routers;
    }

    @Override
    public MessageType getType() {
        return MessageType.PEER_ROUTER_LIST;
    }

    @Override
    public byte[] getWireFormat() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);
            s.writeInt(getType().ordinal());
            s.writeInt(routers.length);
            for (int i = 0; i < routers.length; i++) {
                byte[] bytes = routers[i].getWireFormat();
                s.writeInt(bytes.length);
                s.write(bytes);
            }
            s.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return stream.toByteArray();
    }
}
