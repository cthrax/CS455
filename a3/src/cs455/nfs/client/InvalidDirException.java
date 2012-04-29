package cs455.nfs.client;

public class InvalidDirException extends Exception {

    private final String path;
    private final boolean reserved;
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public InvalidDirException(String path) {
        this(path, false);
    }

    public InvalidDirException(String path, boolean reserved) {
        super("The path " + path + " does not exist.");
        this.reserved = reserved;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public boolean isReserved() {
        return reserved;
    }

}
