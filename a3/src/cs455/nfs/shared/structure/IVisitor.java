package cs455.nfs.shared.structure;

import cs455.nfs.shared.structure.node.CurrentDirNode;
import cs455.nfs.shared.structure.node.DirectoryNode;
import cs455.nfs.shared.structure.node.FileNode;
import cs455.nfs.shared.structure.node.Node;
import cs455.nfs.shared.structure.node.ParentDirNode;

public interface IVisitor {
    void visitNode(Node node);

    void visitFile(FileNode node);

    void visitDirectory(DirectoryNode node);

    void visitParent(ParentDirNode node);

    void visitCur(CurrentDirNode node);
}
