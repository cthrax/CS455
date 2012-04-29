package cs455.nfs.shared.structure.node;

import cs455.nfs.shared.structure.Filesystem;
import cs455.nfs.shared.structure.IVisitor;

public interface INode {
    DirectoryNode getParent();

    String getName();

    void accept(IVisitor visitor);

    Filesystem getFilesystem();

    String getFullPath();
}
