package cdn.shared.message.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cdn.shared.GlobalLogger;
import cdn.shared.message.IMessage;

public class RouterDataMessage implements IMessage {
    int tracker;
    LinkInfo root;
    LinkInfo[] branch;

    public RouterDataMessage(LinkInfo root, LinkInfo[] branch, int tracker) {
        this.root = root;
        this.branch = branch;
        this.tracker = tracker;
    }

    public RouterDataMessage(byte[] bytes) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        try {
            DataInputStream s = new DataInputStream(stream);
            tracker = s.readInt();
            int rootSize = s.readInt();
            byte[] rootBytes = new byte[rootSize];
            s.read(rootBytes, 0, rootSize);
            root = new LinkInfo(rootBytes);
            int branchCount = s.readInt();
            branch = new LinkInfo[branchCount];

            for (int i = 0; i < branchCount; i++) {
                int bSize = s.readInt();
                byte[] branchBytes = new byte[bSize];
                s.read(branchBytes, 0, bSize);
                branch[i] = new LinkInfo(branchBytes);
            }
        } catch (IOException ex) {
            GlobalLogger.debug(this, "Failed to instantiate RouterDataMessage: " + ex.getMessage());
        }
    }

    public int getTracker() {
        return tracker;
    }

    public LinkInfo getRoot() {
        return root;
    }

    public LinkInfo[] getBranch() {
        return branch;
    }

    @Override
    public byte[] getWireFormat() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);
            s.writeInt(getType().ordinal());
            s.writeInt(tracker);
            byte[] rootBytes = root.getWireFormat();
            s.writeInt(rootBytes.length);
            s.write(rootBytes, 0, rootBytes.length);
            s.writeInt(branch.length);
            for (int i = 0; i < branch.length; i++) {
                byte[] bBytes = branch[i].getWireFormat();
                s.writeInt(bBytes.length);
                s.write(bBytes, 0, bBytes.length);
            }

        } catch (IOException ex) {
            GlobalLogger.debug(this, "Failed to get wireformat for RouterDataMessage: " + ex.getMessage());
        }

        return stream.toByteArray();
    }

    @Override
    public MessageType getType() {
        return MessageType.PASSING_DATA;
    }

}
