package cs455.nfs.shared.structure.messages;


public class RmMessage implements IMessage {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final String path;

    public RmMessage(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
