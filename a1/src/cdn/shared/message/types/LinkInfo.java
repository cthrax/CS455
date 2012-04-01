package cdn.shared.message.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cdn.shared.GlobalLogger;
import cdn.shared.message.IMessageElement;

public class LinkInfo implements IMessageElement {
    private final RouterInfo[] routers = new RouterInfo[2];
    private int weight;

    public LinkInfo(int weight, RouterInfo[] routers) {
        this.weight = weight;
        for (int i = 0; i < 2; i++) {
            // TODO: check for invalid number of routers passed in.
            this.routers[i] = routers[i];
        }
    }

    public LinkInfo(byte[] bytes) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        DataInputStream s;
        try {
            s = new DataInputStream(stream);
            weight = s.readInt();

            for (int i = 0; i < 2; i++) {
                int routerSize = s.readInt();
                byte[] reader = new byte[routerSize];
                s.read(reader, 0, routerSize);

                routers[i] = new RouterInfo(reader);

            }
        } catch (IOException e) {
            e.printStackTrace();
            GlobalLogger.warning(this, "Failed to parse LinkInfo");
        }
    }

    public RouterInfo[] getRouters() {
        return routers;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public byte[] getWireFormat() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);

            s.writeInt(weight);
            for (int i = 0; i < 2; i++) {
                byte[] bytes = routers[i].getWireFormat();
                s.writeInt(bytes.length);
                s.write(bytes);
            }

            s.flush();

        } catch (IOException e) {
            GlobalLogger.warning(this, "Failed to parse LinkWeightsUpdateMessage");
        }

        return stream.toByteArray();
    }

}
