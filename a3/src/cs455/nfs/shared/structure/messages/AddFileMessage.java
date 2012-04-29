package cs455.nfs.shared.structure.messages;

public class AddFileMessage implements IMessage {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final String fullPath;
    private final long size;

    public AddFileMessage(String fullPath, long size) {
        this.fullPath = fullPath;
        this.size = size;
    }

    public String getFullPath() {
        return fullPath;
    }

    public long getSize() {
        return size;
    }

}
