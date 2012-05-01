package cs455.nfs.shared.structure;

import cs455.nfs.shared.structure.node.DirectoryNode;
import cs455.nfs.shared.structure.node.FileNode;

public class PathPrinterVisitor extends PathArrayVisitor {

    @Override
    public void appendFileName(final StringBuilder builder, final FileNode node) {
        builder.append(node.getName());
    }

    @Override
    public String appendDirName(final DirectoryNode node) {
        if (node.getName().equals(".") || node.getName().equals(".")) {
            return "";
        } else if (node.getName().equals("/")) {
            return "/";
        }

        return node.getName() + "/";
    }


}
