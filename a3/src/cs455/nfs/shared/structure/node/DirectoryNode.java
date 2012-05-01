package cs455.nfs.shared.structure.node;

import java.util.ListIterator;

import cs455.nfs.client.InvalidDirException;
import cs455.nfs.shared.structure.Filesystem;
import cs455.nfs.shared.structure.IVisitor;
import cs455.nfs.shared.structure.NodeList;

public class DirectoryNode extends Node {
    public DirectoryNode(Filesystem model, DirectoryNode parent, String name) {
        super(model, parent, name);
    }

    public final NodeList<DirectoryNode> dirs = new NodeList<DirectoryNode>();
    public final NodeList<FileNode> files = new NodeList<FileNode>();

    public NodeList<INode> getAllChildren() {
        NodeList<INode> ret = new NodeList<INode>();
        ret.add(0, new ParentDirNode(getFilesystem(), this));
        ret.add(0, new CurrentDirNode(getFilesystem(), this));
        ret.addAll(dirs);
        ret.addAll(files);
        return ret;
    }

    public NodeList<FileNode> getFiles() {
        return files;
    }

    public NodeList<DirectoryNode> getDirectories() {
        NodeList<DirectoryNode> list = new NodeList<DirectoryNode>();
        list.add(new CurrentDirNode(getFilesystem(), this));
        list.add(new ParentDirNode(getFilesystem(), this));
        list.addAll(dirs);
        return list;
    }

    public void addChild(INode node) {
        if (node instanceof DirectoryNode) {
            dirs.add((DirectoryNode) node);
        } else if (node instanceof FileNode) {
            files.add((FileNode) node);
        }
    }

    public void removeChild(INode node) throws InvalidDirException {
        // TODO: Check for cur and parent.
        INode child = getAllChildren().getByName(node.getName());
        if (child != null) {
            ListIterator<DirectoryNode> itr = dirs.listIterator();
            while (itr.hasNext()) {
                DirectoryNode n = itr.next();
                if (n.getFullPath().equals(child.getFullPath())) {
                    itr.remove();
                    break;
                }
            }

            ListIterator<FileNode> itr2 = files.listIterator();
            while (itr2.hasNext()) {
                FileNode n = itr2.next();
                if (n.getFullPath().equals(child.getFullPath())) {
                    itr2.remove();
                    break;
                }
            }

        } else {
            throw new InvalidDirException(node.getFullPath());
        }
    }

    @Override
    public void accept(IVisitor v) {
        v.visitDirectory(this);
    }
}
