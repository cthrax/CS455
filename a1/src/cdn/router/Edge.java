package cdn.router;

import cdn.shared.message.types.LinkInfo;
import cdn.shared.message.types.RouterInfo;

public class Edge implements Comparable<Edge> {
    private final LinkInfo link;
    private boolean isUsed = false;

    public Edge(LinkInfo link) {
        this.link = link;
    }

    public LinkInfo getLink() {
        return link;
    }

    public void setUsed() {
        isUsed = true;
    }

    public boolean isUsed() {
        return isUsed;
    }

    @Override
    public Edge clone() {
        RouterInfo[] routers = new RouterInfo[2];
        RouterInfo r1 = link.getRouters()[0];
        routers[0] = new RouterInfo(r1.getId(), r1.getHostname(), r1.getPort());

        RouterInfo r2 = link.getRouters()[1];
        routers[1] = new RouterInfo(r2.getId(), r2.getHostname(), r2.getPort());

        return new Edge(new LinkInfo(link.getWeight(), routers));
    }

    @Override
    public int compareTo(Edge arg0) {
        int ret = 0;

        if (link.getWeight() < arg0.getLink().getWeight()) {
            ret = -1;
        } else if (link.getWeight() > arg0.getLink().getWeight()) {
            ret = 1;
        }

        return ret;
    }
}
