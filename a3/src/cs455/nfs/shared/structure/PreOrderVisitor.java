package cs455.nfs.shared.structure;

import cs455.nfs.shared.structure.node.CurrentDirNode;
import cs455.nfs.shared.structure.node.DirectoryNode;
import cs455.nfs.shared.structure.node.FileNode;
import cs455.nfs.shared.structure.node.INode;
import cs455.nfs.shared.structure.node.Node;
import cs455.nfs.shared.structure.node.ParentDirNode;

public class PreOrderVisitor implements IVisitor {
    @Override
    public void visitNode(final Node node) {
        System.out.println("default");
    }

    @Override
    public void visitFile(final FileNode node) {
        inFileVisit(node);
    }

    private void defaultAction(final INode node) {
        System.out.println("Default action used. " + node);
    }

    public void outFileVisit(final FileNode node) {
        defaultAction(node);
    }

    public void inFileVisit(final FileNode node) {
        defaultAction(node);
    }

    public void inDirectoryVisit(final DirectoryNode node) {
        defaultAction(node);
    }

    public void outDirectoryVisit(final DirectoryNode node) {
        defaultAction(node);
    }

    @Override
    public void visitDirectory(final DirectoryNode node) {
        if (node == null) {
            return;
        }
        inDirectoryVisit(node);
        for (FileNode n : node.getFiles()) {
            n.accept(this);
        }

        for (DirectoryNode n : node.getDirectories()) {
            n.accept(this);
        }

        outDirectoryVisit(node);
    }

    @Override
    public void visitParent(final ParentDirNode node) {
        // No-op
    }

    @Override
    public void visitCur(final CurrentDirNode node) {
        // No-op
    }

}
