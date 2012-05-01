package cs455.nfs.shared.structure.node;

import cs455.nfs.shared.structure.Filesystem;
import cs455.nfs.shared.structure.IVisitor;


public class FileNode extends Node {

    public FileNode(Filesystem model, DirectoryNode parent, String name) {
        super(model, parent, name);
    }

    @Override
    public void accept(IVisitor v) {
        v.visitFile(this);
    }

}
