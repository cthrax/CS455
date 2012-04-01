package cdn.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cdn.shared.message.types.LinkInfo;
import cdn.shared.message.types.RouterInfo;

public class RoutingPlan {
    Map<String, List<LinkInfo>> branches = new HashMap<String, List<LinkInfo>>();
    List<LinkInfo> immediates = new ArrayList<LinkInfo>();

    public RoutingPlan(LinkInfo[] mst, RouterInfo root) {
        List<LinkInfo> master = new ArrayList<LinkInfo>();
        for (int i = 0; i < mst.length; i++) {
            master.add(mst[i]);
        }

        int count = 0;
        while (count < master.size()) {
            LinkInfo cur = master.get(count);
            if (cur.getRouters()[0].getId().equals(root.getId())) {
                immediates.add(master.remove(count));
            } else if (cur.getRouters()[1].getId().equals(root.getId())) {
                RouterInfo[] newRouters = new RouterInfo[2];
                RouterInfo[] oldRouters = cur.getRouters();
                newRouters[0] = oldRouters[1];
                newRouters[1] = oldRouters[0];
                LinkInfo reversed = new LinkInfo(cur.getWeight(), newRouters);
                master.remove(count);
                immediates.add(reversed);
            } else {
                count++;
            }
        }

        for (int i = 0; i < immediates.size(); i++) {
            LinkInfo cur = immediates.get(i);
            branches.put(cur.getRouters()[1].getId(), createBranches(new ArrayList<LinkInfo>(), master, cur.getRouters()[1]));
        }
    }

    private List<LinkInfo> createBranches(List<LinkInfo> branch, List<LinkInfo> list, RouterInfo root) {
        int count = 0;
        while (count < list.size()) {
            LinkInfo cur = list.get(count);
            if (cur.getRouters()[0].getId().equals(root.getId())) {
                branch.add(list.remove(count));
                createBranches(branch, list, cur.getRouters()[1]);
            } else if (cur.getRouters()[1].getId().equals(root.getId())) {
                branch.add(list.remove(count));
                createBranches(branch, list, cur.getRouters()[0]);
            } else {
                count++;
            }
        }

        return branch;
    }

    public LinkInfo[] getImmediates() {
        return immediates.toArray(new LinkInfo[0]);
    }

    public LinkInfo[] getBranchPlan(LinkInfo branch) {
        return branches.get(branch.getRouters()[1].getId()).toArray(new LinkInfo[0]);
    }
}
