package cs455.nfs.shared.structure.messages;


public class MoveMessage implements IMessage {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final File src;
    private final File dest;

    public File getSrc() {
        return src;
    }

    public File getDest() {
        return dest;
    }

    public MoveMessage(File src, File dest) {
        this.src = src;
        this.dest = dest;
    }
}
