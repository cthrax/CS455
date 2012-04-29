package cs455.nfs.shared.structure.messages;


public class AddFolderMessage implements IMessage {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String folderPath;

    public AddFolderMessage(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

}
