package cs455.nfs.shared.structure;

import java.util.Deque;
import java.util.LinkedList;

import cs455.nfs.client.IServiceModel;
import cs455.nfs.client.InvalidDirException;
import cs455.nfs.shared.structure.node.CurrentDirNode;
import cs455.nfs.shared.structure.node.DirectoryNode;
import cs455.nfs.shared.structure.node.FileNode;
import cs455.nfs.shared.structure.node.INode;
import cs455.nfs.shared.structure.node.ParentDirNode;

public class Filesystem {
    private final IServiceModel model;

    private DirectoryNode root;

    public Filesystem(final IServiceModel model) {
        this.model = model;
    }

    public void addSerializedNodes(final String[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            createPaths(nodes[i], true);
        }
    }

    private INode createPaths(final String fullPath, final boolean typed) {
        String[] split = fullPath.split("/");
        DirectoryNode curPos = root;

        for (int j = 0; j < split.length; j++) {
            if (split[j].equals("")) {
                if (!typed && root == null) {
                    root = new DirectoryNode(this, null, "/");
                }
                continue;
            }
            Character type;
            String name;
            if (typed) {
                type = split[j].charAt(0);
                name = split[j].substring(1);
            } else {
                type = 'D';
                name = split[j];
            }

            if (type == 'D') {
                if (name.equals(".")) {
                    continue;
                } else if (name.equals("..")) {
                    curPos = curPos.getParent();
                    continue;
                } else if (name.equals("")) {
                    if (root == null) {
                        root = new DirectoryNode(this, null, "/");
                    }
                    curPos = root;
                    continue;
                }

                DirectoryNode newNode = null;
                if (curPos != null) {
                    newNode = curPos.getDirectories().getByName(name);
                }

                if (newNode == null) {
                    newNode = new DirectoryNode(this, curPos, name);
                    if (curPos != null) {
                        curPos.addChild(newNode);
                    }
                }

                curPos = newNode;
            } else {
                if (root == null) {
                    root = new DirectoryNode(this, null, "/");
                    curPos = root;
                }
                curPos.addChild(new FileNode(this, curPos, name));
            }
        }
        return curPos;
    }

    public IServiceModel getServiceModel() {
        return model;
    }

    public void addPath(final String path) {
        INode n = createPaths(path, false);
        model.addDir(n.getParent());
    }

    public void addNode(final INode node) throws InvalidDirException {
        String name = node.getName();
        if (name != null && (name.equals(".") || name.equals("..")) || name == null || node.getParent() == null) {
            throw new InvalidDirException(name, true);
        } else if (name.contains("/")) {
            throw new InvalidDirException(name, true);
        }

        // TODO: Error handling
        model.addDir(node);
        node.getParent().addChild(node);
    }

    public DirectoryNode getRoot() {
        return root;
    }

    public void mv(final INode src, final INode dst) throws Exception {
        // TODO: Error handling
        model.mvFile(src, dst);
        try {
            src.getParent().removeChild(src);
        } catch (InvalidDirException e) {
            System.out.println("Unable to complete move because " + e.getMessage());
        }

        dst.getParent().addChild(dst);
    }

    public void rmDir(final INode node) throws InvalidDirException {
        if (node == null || node instanceof CurrentDirNode || node instanceof ParentDirNode) {
            throw new InvalidDirException(node.getName(), true);
        } else if (!(node instanceof DirectoryNode)) {
            throw new InvalidDirException(node.getName(), false);
        }

        // TODO: Error handling
        model.rmDir(node);
        node.getParent().removeChild(node);
    }

    public void rmFile(final INode node) throws InvalidDirException {
        model.rmDir(node);
        node.getParent().removeChild(node);
    }

    public INode findByPath(final String args) throws Exception {
        if (args != null && args.equals("/")) {
            return root;
        } else if (args == null) {
            throw new InvalidDirException(args, false);
        }

        String[] split = args.split("/");
        // Get implicit start.
        INode cur = root;
        if (cur == null) {
            System.out.println("Couldn't find root!");
            return null;
        }

        Deque<INode> nodeStack = new LinkedList<INode>();
        // nodeStack.push(root);
        nodeStack.push(cur);
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("")) {
                continue;
            }

            if (cur instanceof DirectoryNode) {
                DirectoryNode d = (DirectoryNode) cur;
                cur = d.getAllChildren().getByName(split[i]);
                nodeStack.push(cur);
                if (cur == null) {
                    throw new Exception("No such path!");
                } else if (cur instanceof ParentDirNode) {
                    nodeStack.pop();
                    nodeStack.pop();
                } else if (cur instanceof CurrentDirNode) {
                    nodeStack.pop();
                }

            } else if (cur instanceof FileNode) {
                throw new Exception(split[i] + " is a file!");
            }
        }

        return nodeStack.pop();
    }
}
