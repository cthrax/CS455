package cs455.nfs.shared.structure;

import java.util.ArrayList;

import cs455.nfs.shared.structure.node.INode;

public class NodeList<E extends INode> extends ArrayList<E> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public E getByName(String name) {
        for (E cur : this) {
            if (cur.getName().equals(name)) {
                return cur;
            }
        }

        return null;
    }

}
