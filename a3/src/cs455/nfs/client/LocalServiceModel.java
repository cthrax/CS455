package cs455.nfs.client;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import cs455.nfs.shared.structure.Filesystem;
import cs455.nfs.shared.structure.node.DirectoryNode;
import cs455.nfs.shared.structure.node.FileNode;
import cs455.nfs.shared.structure.node.INode;

public class LocalServiceModel implements IServiceModel {
    private final File realRoot;

    public LocalServiceModel(final File realRoot) {
        this.realRoot = realRoot;
    }

    @Override
    public void addDir(final INode node) {
        System.out.println("Creating local dir.");
        File newDir = getRealFile(node.getFullPath());
        if (!newDir.exists()) {
            // TODO: Check for errors.
            if (!newDir.mkdirs()) {
                System.out.println("Failed to create directory: " + newDir.getAbsolutePath());
            } else {
                System.out.println("Success!");
            }
        }
    }

    @Override
    public void rmDir(final INode node) {
        File oldDir = getRealFile(node.getFullPath());
        // TODO: Check for errors.
        oldDir.delete();
    }

    @Override
    public void mvFile(final INode src, final INode dest) {
        File rsrc = getRealFile(src.getFullPath());
        File rdst = getRealFile(dest.getFullPath());
        rsrc.renameTo(rdst);
    }

    @Override
    public Filesystem peek() {
        Filesystem system = new Filesystem(this);
        appendToTree(realRoot, system.getRoot());
        return system;
    }

    public File getRealFile(final String path) {
        return new File(realRoot.getAbsolutePath() + path);
    }

    private void appendToTree(final File curFile, final DirectoryNode curNode) {
        for (File file : curFile.listFiles()) {
            if (file.getName().equals(".") || file.getName().equals("..")) {
                continue;
            } else {
                if (file.isFile()) {
                    curNode.addChild(new FileNode(curNode.getFilesystem(), curNode, file.getName()));
                } else {
                    DirectoryNode newNode = new DirectoryNode(curNode.getFilesystem(), curNode, file.getName());
                    curNode.addChild(newNode);
                    appendToTree(file, newNode);
                }
            }
        }
    }

    @Override
    public String getHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "";
        }
    }

    @Override
    public int getPort() {
        return 0;
    }

}
