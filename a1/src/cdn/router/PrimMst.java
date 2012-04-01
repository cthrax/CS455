package cdn.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import cdn.shared.message.types.LinkInfo;
import cdn.shared.message.types.RouterInfo;

public class PrimMst implements IMST {
    private final PriorityQueue<Edge> edges = new PriorityQueue<Edge>();
    private final HashMap<String, Node> nodes = new HashMap<String, Node>();
    private final Map<String, ArrayList<Edge>> graph = new HashMap<String, ArrayList<Edge>>();
    private final Map<String, ArrayList<Edge>> rgraph = new HashMap<String, ArrayList<Edge>>();

    public PrimMst(LinkInfo[] links) {
        Map<String, RouterInfo> routerMap = new HashMap<String, RouterInfo>();

        for (int i = 0; i < links.length; i++) {
            String left = links[i].getRouters()[0].getId();
            String right = links[i].getRouters()[1].getId();
            routerMap.put(left, links[i].getRouters()[0]);
            routerMap.put(right, links[i].getRouters()[1]);

            if (graph.get(left) == null) {
                graph.put(left, new ArrayList<Edge>());
            }

            if (rgraph.get(right) == null) {
                rgraph.put(right, new ArrayList<Edge>());
            }

            Edge edge = new Edge(links[i]);
            graph.get(left).add(edge);
            rgraph.get(right).add(edge);
        }

        RouterInfo[] routers = new ArrayList<RouterInfo>(routerMap.values()).toArray(new RouterInfo[0]);
        for (int i = 0; i < routers.length; i++) {
            nodes.put(routers[i].getId(), new Node(routers[i]));
        }
    }

    @Override
    public LinkInfo[] execute() {
        ArrayList<LinkInfo> path = new ArrayList<LinkInfo>();

        Node first = nodes.values().iterator().next();
        first.setSelected();
        addNodeEdges(first);

        Edge cur;
        while (hasUnconnectedNodes()) {
            cur = edges.poll();

            if (!cur.isUsed() && getHeadByEdge(cur) != null && !getHeadByEdge(cur).isSelected()) {
                cur.setUsed();
                addLowest(cur, getHeadByEdge(cur), path);

            } else if (!cur.isUsed() && getTailByEdge(cur) != null && !getTailByEdge(cur).isSelected()) {
                cur.setUsed();
                addLowest(cur, getTailByEdge(cur), path);

            } else {
                cur.setUsed();
                edges.remove(cur);
            }
        }

        return path.toArray(new LinkInfo[0]);
    }

    private void addLowest(Edge cur, Node next, List<LinkInfo> path) {
        next.setSelected();
        edges.remove(cur);
        path.add(cur.getLink());
        addNodeEdges(next);
    }

    private Node getHeadByEdge(Edge edge) {
        return nodes.get(edge.getLink().getRouters()[1].getId());
    }

    private Node getTailByEdge(Edge edge) {
        return nodes.get(edge.getLink().getRouters()[0].getId());
    }

    private boolean hasUnconnectedNodes() {
        for (String key : nodes.keySet()) {
            if (!nodes.get(key).isSelected()) {
                return true;
            }
        }

        return false;
    }

    private void addNodeEdges(Node node) {
        List<Edge> edges1 = graph.get(node.getRouter().getId());
        List<Edge> edges2 = rgraph.get(node.getRouter().getId());
        if (edges2 == null) {
            edges2 = new ArrayList<Edge>();
        }

        if (edges1 == null) {
            edges1 = new ArrayList<Edge>();
        }

        for (int i = 0; i < edges2.size(); i++) {
            edges1.add(edges2.get(i));
        }


        for (int i = 0; i < edges1.size(); i++) {
            if (!edges1.get(i).isUsed()) {
                edges.add(edges1.get(i));
            }
        }
    }
}
