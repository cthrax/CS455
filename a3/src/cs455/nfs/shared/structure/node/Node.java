package cs455.nfs.shared.structure.node;

import cs455.nfs.shared.structure.Filesystem;
import cs455.nfs.shared.structure.IVisitor;

public class Node implements INode {

    private final Filesystem model;
    private final DirectoryNode parent;
    private final String name;

    public Node(Filesystem model, DirectoryNode parent, String name) {
        this.model = model;
        this.parent = parent;
        this.name = name;

    }

    @Override
    public Filesystem getFilesystem() {
        return model;
    }

    @Override
    public DirectoryNode getParent() {
        return parent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitNode(this);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getFullPath() {
        StringBuilder builder = new StringBuilder();
        INode node = this;
        while (node.getParent() != null) {
            builder.insert(0, node.getName());
            builder.insert(0, "/");
            node = node.getParent();
        }

        return builder.toString();
    }

    @Override
    public int hashCode() {
        return getFullPath().hashCode();
    }
}
