package cs455.nfs.shared.structure.messages;

import cs455.nfs.shared.structure.node.FileNode;
import cs455.nfs.shared.structure.node.INode;


public class File implements IMessage {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final String path;
    private final String host;
    private final int port;
    private boolean isFile = false;

    public File(String path, String host, int port, boolean isFile) {
        this.path = path;
        this.host = host;
        this.port = port;
        this.isFile = isFile;
    }

    public File(INode node) {
        path = node.getFullPath();
        host = node.getFilesystem().getServiceModel().getHost();
        port = node.getFilesystem().getServiceModel().getPort();
        isFile = node instanceof FileNode;
    }

    public String getPath() {
        return path;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isFile() {
        return isFile;
    }
}
