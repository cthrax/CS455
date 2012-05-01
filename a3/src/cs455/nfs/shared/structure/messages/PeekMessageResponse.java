package cs455.nfs.shared.structure.messages;


public class PeekMessageResponse implements IMessage {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final String[] files;

    public PeekMessageResponse(String[] files) {
        this.files = files;
    }

    public String[] getFiles() {
        return files;
    }

}
