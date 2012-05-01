package cs455.nfs.client;

import cs455.nfs.shared.structure.Filesystem;
import cs455.nfs.shared.structure.node.INode;

public interface IServiceModel {
    public String getHost();

    public int getPort();

    public void addDir(INode node);

    public void rmDir(INode node);

    public void mvFile(INode src, INode dest) throws Exception;

    public Filesystem peek();
}
