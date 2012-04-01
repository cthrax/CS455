package cdn.shared.message.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cdn.shared.GlobalLogger;
import cdn.shared.message.IMessage;

public class LinkWeightsUpdateMessage implements IMessage {

    private final MessageType type = MessageType.LINK_WEIGHT_UPDATE;
    private int numberOfLinks = 0;
    private LinkInfo[] links;

    public LinkWeightsUpdateMessage(LinkInfo[] links) {
        numberOfLinks = links.length;
        this.links = links;
    }

    public LinkWeightsUpdateMessage(byte[] bytes) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        DataInputStream s;
        try {
            s = new DataInputStream(stream);
            numberOfLinks = s.readInt();
            links = new LinkInfo[numberOfLinks];

            for (int i = 0; i < numberOfLinks; i++) {
                int linkSize = s.readInt();
                byte[] reader = new byte[linkSize + 1];
                s.read(reader, 0, linkSize);
                links[i] = new LinkInfo(reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
            GlobalLogger.warning(this, "Failed to parse LinkWeightsUpdateMessage");
        }
    }

    @Override
    public byte[] getWireFormat() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);
            s.writeInt(getType().ordinal());
            s.writeInt(numberOfLinks);
            for (int i = 0; i < numberOfLinks; i++) {
                byte[] bytes = links[i].getWireFormat();
                s.writeInt(bytes.length);
                s.write(bytes);
            }

            s.flush();
        } catch (IOException e) {
            e.printStackTrace();
            GlobalLogger.warning(this, "Failed to getWireFormat for LinkWeightsUpdateMessage");
        }
        return stream.toByteArray();
    }

    public int getLinkCount() {
        return numberOfLinks;
    }

    public LinkInfo[] getLinks() {
        return links;
    }

    @Override
    public MessageType getType() {
        return type;
    }

}
