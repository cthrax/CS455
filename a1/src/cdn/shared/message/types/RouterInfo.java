package cdn.shared.message.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cdn.shared.GlobalLogger;
import cdn.shared.message.IMessageElement;


public class RouterInfo implements IMessageElement {
    private String id;
    private String hostname;
    private int port;

    public RouterInfo(String id, String hostname, int port) {
        this.id = id;
        this.hostname = hostname;
        this.port = port;
    }

    public RouterInfo(byte[] bytes) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        DataInputStream s;
        try {
            s = new DataInputStream(stream);
            id = s.readUTF();
            hostname = s.readUTF();
            port = s.readInt();
        } catch (IOException e) {
            GlobalLogger.warning(this, "Failed to parse LinkWeightsUpdateMessage");
        }
    }

    @Override
    public byte[] getWireFormat() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);
            s.writeUTF(id);
            s.writeUTF(hostname);
            s.writeInt(port);
            s.flush();

        } catch (IOException e) {
            GlobalLogger.warning(this, "Failed to parse LinkWeightsUpdateMessage");
        }

        return stream.toByteArray();
    }

    public String getId() {
        return id;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
