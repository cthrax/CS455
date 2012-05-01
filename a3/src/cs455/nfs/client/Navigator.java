package cs455.nfs.client;

import java.util.LinkedList;
import java.util.ListIterator;

import cs455.nfs.shared.structure.Filesystem;
import cs455.nfs.shared.structure.node.DirectoryNode;
import cs455.nfs.shared.structure.node.FileNode;
import cs455.nfs.shared.structure.node.INode;

/**
 * This class is responsible for maintaining navigation within the virtual filesystem and by extension of that,
 * the entry point for most operations on the fileystem since they require knowledge of the current position in the filesystem.
 *
 * @author myles
 *
 */
public class Navigator {
    /**
     * This is the stack that maintains position as one moves through the filesystem.
     */
    private final LinkedList<DirectoryNode> path = new LinkedList<DirectoryNode>();

    public Navigator() {
        path.push(new DirectoryNode(new Filesystem(new VirtualServiceModel()), null, "/"));
    }

    /**
     * Prints the current working directory.
     */
    public void printWorkingDirectory() {
        StringBuilder builder = new StringBuilder();
        boolean skip = true;
        ListIterator<DirectoryNode> itr = path.listIterator();
        // Move to end
        while (itr.hasNext()) {
            itr.next();
        }
        while (itr.hasPrevious()) {
            DirectoryNode cur = itr.previous();
            // Don't need to append "/" for first one
            if (skip) {
                skip = false;
                continue;
            }
            builder.append("/");
            builder.append(cur.getName());
        }

        if (builder.length() == 0) {
            builder.append("/");
        }
        System.out.println(builder.toString());
    }

    /**
     * This moves a file from the given src to a destination.
     *
     * @param src the source.
     * @param dest the destination.
     * @throws InvalidDirException If an invalid directory is passed in as either source or destination then this can be thrown.
     * @throws Exception this occurrs for any reason taht would prevent the algorithm from moving forward.
     */
    public void moveFile(final String src, final String dest) throws InvalidDirException, Exception {
        // Get copies of the current path so that the long paths passed in can be navigated.
        @SuppressWarnings("unchecked")
        LinkedList<DirectoryNode> spath = (LinkedList<DirectoryNode>) path.clone();
        @SuppressWarnings("unchecked")
        LinkedList<DirectoryNode> dpath = (LinkedList<DirectoryNode>) path.clone();

        // ******************
        // Navigate to the source node.
        String[] split = src.split("/");
        String filename = split[split.length - 1];
        boolean isFile = false;
        try {
            changeDirs(spath, split);
        } catch (InvalidDirException e) {
            if (spath.peek().equals("/") || split.length >= 2 && spath.peek().getName().equals(split[split.length - 2])) {
                throw e;
            }
            isFile = true;
        }

        if (!isFile) {
            throw new Exception("Source must be a file.");
        }

        // **************************
        // Navigate to the destination node.
        INode nsrc = spath.peek().getFiles().getByName(split[split.length - 1]);
        split = dest.split("/");
        boolean isRenaming = false;

        try {
            changeDirs(dpath, split);
        } catch (InvalidDirException e) {
            if (spath.peek().equals("/") || split.length >= 2 && spath.peek().getName().equals(split[split.length - 2])) {
                throw e;
            }
            isRenaming = true;
        }

        // This allows the user to pass in a new name for the file once it is moved.
        String newNodeName = filename;
        if (isRenaming) {
            String newFileName = split[split.length - 1];
            if (dpath.peek().getFiles().getByName(newFileName) != null) {
                throw new Exception("Attempting to rename file to an existing file. Aborting.");
            } else {
                newNodeName = newFileName;
            }
        }

        DirectoryNode dfolder = dpath.peek();
        if (dfolder.getFiles().getByName(newNodeName) != null) {
            throw new Exception("A file with that name already exists.");
        }

        /*
         * This takes a little bit to wrap your head around. Basically parents on remote nodes are not the same as the
         * children in the filesystem. So the virtual filesystem can have children that have different parents. This way a full
         * remote path can be obtained easily, but in situations like this, the remote mount points have to be maintained and if the
         * virtual fs is the current parent, then the new parent needs to be calculated accordingly.
         */
        INode newNode = null;
        if (dfolder.getFilesystem().getServiceModel() instanceof VirtualServiceModel) {
            VirtualServiceModel m = (VirtualServiceModel) dfolder.getFilesystem().getServiceModel();
            if (m.isMounted(dfolder)) {
                newNode = new FileNode(m.getMountPoint(dfolder), dfolder.getAllChildren().get(2).getParent(), newNodeName);
            } else {
                throw new Exception("Files can only be moved within directory services, virtual filesystems cannot receive files.");
            }
        } else {
            newNode = new FileNode(dfolder.getFilesystem(), dfolder, newNodeName);
        }

        try {
            nsrc.getFilesystem().mv(nsrc, newNode);
            INode n = ((VirtualServiceModel) path.peekLast().getFilesystem().getServiceModel()).getMountParent(nsrc);
            if (n != null) {
                if (n instanceof DirectoryNode) {
                    ((DirectoryNode) n).removeChild(nsrc);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void changeDir(final String name) throws InvalidDirException {
        changeDir(path, name);
    }

    private void changeDirs(final LinkedList<DirectoryNode> nodes, final String[] dirs) throws InvalidDirException {
        for (int i = 0; i < dirs.length; i++) {
            changeDir(nodes, dirs[i]);
        }
    }

    /**
     * This changes the passed in stack according to the multi-part path that was passed in.
     * 
     * @param path the stack that needs to be updated.
     * @param name the path that needs to be interpreted.
     * @throws InvalidDirException
     */
    private void changeDir(final LinkedList<DirectoryNode> path, final String name) throws InvalidDirException {
        if (name.equals(".")) {
            return;
        } else if (name.equals("..")) {
            if (!path.peek().getName().equals("/")) {
                path.pop();
            } else {
                throw new InvalidDirException(name);
            }
        } else if (path.peek() != null && path.peek().getDirectories().getByName(name) != null) {
            path.push(path.peek().getDirectories().getByName(name));
        } else {
            throw new InvalidDirException(name);
        }
    }

    /**
     * Mounts a remote filesystem.
     * 
     * @param nodes the attach point of the remote filesystem. This is where the parent and child relation diverges.
     */
    public void mountDir(final INode nodes) {
        if (path.peek().getFilesystem().getServiceModel() instanceof VirtualServiceModel) {
            VirtualServiceModel model = (VirtualServiceModel) path.peek().getFilesystem().getServiceModel();
            for (INode node : ((DirectoryNode) nodes).getAllChildren()) {
                if (!node.getName().equals(".") && !node.getName().equals("..")) {
                    path.peek().addChild(node);
                }
            }
            model.setMountPoint(path.peek(), nodes.getFilesystem());
        } else {
            System.out.println("You may not mount within a mounted directory.");
        }
    }

    /**
     * Remove a directory.
     * 
     * @param name
     * @throws InvalidDirException
     */
    public void rmDir(final String name) throws InvalidDirException {
        INode node = path.peek().getAllChildren().getByName(name);
        node.getFilesystem().getServiceModel().rmDir(node);
        node.getParent().removeChild(node);
        path.peek().getFilesystem().rmDir(node);
    }

    /**
     * Add a directory.
     * 
     * @param name
     * @throws InvalidDirException
     */
    public void addDir(final String name) throws InvalidDirException {
        if (path.peek().getFilesystem().getServiceModel() instanceof VirtualServiceModel) {
            VirtualServiceModel m = (VirtualServiceModel) path.peek().getFilesystem().getServiceModel();
            if (m.isMounted(path.peek())) {
                Filesystem mp = m.getMountPoint(path.peek());
                INode newDir = new DirectoryNode(mp, path.peek().getAllChildren().get(2).getParent(), name);
                mp.addPath(name);
                mp.getServiceModel().addDir(newDir);
                path.peek().addChild(newDir);
            } else {
                INode newDir = new DirectoryNode(path.peek().getFilesystem(), path.peek(), name);
                path.peek().getFilesystem().getServiceModel().addDir(newDir);
                path.peek().addChild(newDir);
            }
        } else {
            INode newDir = new DirectoryNode(path.peek().getFilesystem(), path.peek(), name);
            path.peek().getFilesystem().getServiceModel().addDir(newDir);
            path.peek().addChild(newDir);
        }
    }

    /**
     * List all files or directories in the current working directory.
     */
    public void list() {
        for (INode node : path.peek().getDirectories()) {
            System.out.println(node.getName());
        }

        for (INode node : path.peek().getFiles()) {
            System.out.println(node.getName());
        }
    }

}
