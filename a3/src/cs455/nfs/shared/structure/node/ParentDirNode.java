package cs455.nfs.shared.structure.node;

import cs455.nfs.shared.structure.Filesystem;
import cs455.nfs.shared.structure.IVisitor;

public class ParentDirNode extends DirectoryNode {

    public ParentDirNode(Filesystem model, DirectoryNode parent) {
        super(model, parent, "..");
    }

    @Override
    public void accept(IVisitor v) {
        v.visitParent(this);
    }

}
