package cs455.nfs.shared.structure.messages;


public class Success implements IMessage {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public String getMessage() {
        return message;
    }

    private final String message;

    public Success(String message) {
        this.message = message;
    }

}
