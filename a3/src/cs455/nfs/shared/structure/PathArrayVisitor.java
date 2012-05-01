package cs455.nfs.shared.structure;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import cs455.nfs.shared.structure.node.DirectoryNode;
import cs455.nfs.shared.structure.node.FileNode;

public class PathArrayVisitor extends PreOrderVisitor {
    Deque<String> path = new LinkedList<String>();
    List<String> paths = new LinkedList<String>();

    @Override
    public void inFileVisit(final FileNode node) {
        String[] p = path.toArray(new String[0]);
        StringBuilder builder = new StringBuilder();

        for (int i = p.length - 1; i >= 0; i--) {
            builder.append(p[i]);
        }
        appendFileName(builder, node);
        paths.add(builder.toString());
    }

    public void appendFileName(final StringBuilder builder, final FileNode node) {
        builder.append("F" + node.getName());
    }

    public String appendDirName(final DirectoryNode node) {
        if (node.getName().equals(".") || node.getName().equals("..")) {
            return "";
        } else if (node.getName().equals("") || node.getName().equals("/")) {
            return "D/";
        }
        return "D" + node.getName() + "/";
    }

    @Override
    public void inDirectoryVisit(final DirectoryNode node) {
        if (node == null) {
            return;
        }
        // Append empty directories
        if (node.getAllChildren().size() == 0) {
            String[] p = path.toArray(new String[0]);
            StringBuilder builder = new StringBuilder();

            for (int i = p.length - 1; i >= 0; i--) {
                builder.append(p[i]);
            }
            builder.append(appendDirName(node));
            paths.add(builder.toString());
        }

        path.push(appendDirName(node));
    }

    @Override
    public void outDirectoryVisit(final DirectoryNode node) {
        path.pop();
    }

    public String[] getPathArray() {
        return paths.toArray(new String[0]);
    }
}
