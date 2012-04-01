package cdn.shared;

import cdn.router.RoutingPlan;
import cdn.shared.message.types.LinkInfo;
import cdn.shared.message.types.RouterInfo;

public class MessagePrinter {
    public static String print(RoutingPlan plan, RouterInfo root) {
        return print(plan, root, new StringBuilder()).toString();
    }

    public static StringBuilder print(RoutingPlan plan, RouterInfo root, StringBuilder builder) {
        builder.append(root.getId());
        builder.append(" --- ");
        int spaceCount = 5 + root.getId().length();
        LinkInfo[] immediates = plan.getImmediates();
        for (int i = 0; i < immediates.length; i++) {
            if (i > 0) {
                builder.append("\n");
                for (int j = 0; j < spaceCount; j++) {
                    builder.append(" ");
                }
            }
            LinkInfo cur = immediates[i];
            RouterInfo newRoot = cur.getRouters()[1];
            builder.append(cur.getWeight());
            rprint(new RoutingPlan(plan.getBranchPlan(cur), newRoot), newRoot, builder, spaceCount + (cur.getWeight() + "").length());
        }

        return builder;
    }

    private static StringBuilder rprint(RoutingPlan plan, RouterInfo root, StringBuilder builder, int spaceCount) {
        builder.append(" --- ");
        builder.append(root.getId());
        spaceCount += 5;
        spaceCount += root.getId().length();
        LinkInfo[] immediates = plan.getImmediates();
        for (int i = 0; i < immediates.length; i++) {
            if (i > 0) {
                builder.append("\n");
                for (int j = 0; j < spaceCount; j++) {
                    builder.append(" ");
                }
            }
            RouterInfo newRoot = immediates[i].getRouters()[1];
            builder.append(" --- ");
            builder.append(immediates[i].getWeight());
            rprint(new RoutingPlan(plan.getBranchPlan(immediates[i]), newRoot), newRoot, builder, spaceCount);
        }

        return builder;
    }

    public static String print(LinkInfo[] links) {
        return print(links, new StringBuilder()).toString();
    }

    public static StringBuilder print(LinkInfo[] links, StringBuilder builder) {
        for (int i = 0; i < links.length; i++) {
            LinkInfo cur = links[i];
            print(cur, builder);
        }
        return builder;
    }

    public static String print(LinkInfo link) {
        return print(link, new StringBuilder()).toString();
    }

    public static StringBuilder print(LinkInfo link, StringBuilder builder) {
        print(link.getRouters()[0], builder);
        builder.append(" -- ");
        builder.append(link.getWeight());
        builder.append(" -- ");
        print(link.getRouters()[1], builder);
        builder.append("\n");
        return builder;
    }

    public static String print(RouterInfo[] routers) {
        return print(routers, new StringBuilder()).toString();
    }

    public static StringBuilder print(RouterInfo[] routers, StringBuilder builder) {
        for (int i = 0; i < routers.length; i++) {
            print(routers[i], builder);
            builder.append("\n");
        }
        return builder;
    }

    public static String print(RouterInfo router) {
        return print(router, new StringBuilder()).toString();
    }

    public static StringBuilder print(RouterInfo router, StringBuilder builder) {
        builder.append(router.getId());
        builder.append(" (");
        builder.append(router.getHostname());
        builder.append(":");
        builder.append(router.getPort());
        builder.append(")");
        return builder;
    }
}
