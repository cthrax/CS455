package cs455.nfs.client;

import java.util.HashMap;
import java.util.Map;

import cs455.nfs.shared.structure.Filesystem;
import cs455.nfs.shared.structure.node.DirectoryNode;
import cs455.nfs.shared.structure.node.INode;

public class VirtualServiceModel implements IServiceModel {
    private final Map<INode, Filesystem> mountPoints = new HashMap<INode, Filesystem>();

    public VirtualServiceModel() {
    }

    @Override
    public void addDir(final INode node) {
    }

    @Override
    public void rmDir(final INode node) {
    }

    @Override
    public void mvFile(final INode src, final INode dest) {
    }

    @Override
    public Filesystem peek() {
        return null;
    }

    @Override
    public String getHost() {
        return "";
    }

    @Override
    public int getPort() {
        return 0;
    }

    public void setMountPoint(final INode node, final Filesystem fs) {
        mountPoints.put(node, fs);
    }

    public boolean isMounted(final INode node) {
        return mountPoints.get(node) != null;
    }

    public Filesystem getMountPoint(final INode node) {
        return mountPoints.get(node);
    }

    public INode getMountParent(final INode target) {
        for (INode node : mountPoints.keySet()) {
            if (node instanceof DirectoryNode) {
                DirectoryNode cur = (DirectoryNode) node;

                if (cur.getAllChildren().getByName(target.getName()) != null) {
                    INode test = cur.getAllChildren().getByName(target.getName());
                    if (test.getFullPath().equals(target.getFullPath())) {
                        return cur;
                    }
                }
            }
        }

        return null;
    }

}
