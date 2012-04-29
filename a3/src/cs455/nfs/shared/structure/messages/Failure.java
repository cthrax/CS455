package cs455.nfs.shared.structure.messages;


public class Failure implements IMessage {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final String message;

    public String getMessage() {
        return message;
    }

    public Failure(String message) {
        this.message = message;
    }
}
