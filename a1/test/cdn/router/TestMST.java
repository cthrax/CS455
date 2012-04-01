package cdn.router;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.testng.annotations.Test;

import cdn.shared.message.types.LinkInfo;
import cdn.shared.message.types.RouterInfo;

public class TestMST {
    @Test
    public void testMst() {
        LinkInfo[] test = new LinkInfo[6];
        RouterInfo[] routers = createRouters(4);

        test[0] = new LinkInfo(1, new RouterInfo[] { routers[0], routers[2] });
        test[1] = new LinkInfo(2, new RouterInfo[] { routers[0], routers[3] });
        test[2] = new LinkInfo(3, new RouterInfo[] { routers[1], routers[0] });
        test[3] = new LinkInfo(2, new RouterInfo[] { routers[1], routers[3] });
        test[4] = new LinkInfo(1, new RouterInfo[] { routers[2], routers[3] });
        test[5] = new LinkInfo(4, new RouterInfo[] { routers[2], routers[1] });

        PrimMst stg = new PrimMst(test);
        LinkInfo[] mst = stg.execute();

        assertEquals(mst.length, 3);

        ArrayList<LinkInfo> list = new ArrayList<LinkInfo>();
        for (int i = 0; i < mst.length; i++) {
            list.add(mst[i]);
            System.out.println(mst[i].getRouters()[0].getId() + ", " + mst[i].getRouters()[1].getId());
        }

        Collections.sort(list, new Comparator<LinkInfo>() {
            @Override
            public int compare(LinkInfo o1, LinkInfo o2) {
                int t = o1.getRouters()[0].getId().compareTo(o2.getRouters()[0].getId()) * -1;

                int left;
                String[] split1 = o1.getRouters()[0].getId().split("-");
                left = Integer.parseInt(split1[1]);

                int right;
                String[] split2 = o2.getRouters()[0].getId().split("-");
                right = Integer.parseInt(split2[1]);
                if (left < right) {
                    return -1;
                } else if (left > right) {
                    return 1;
                } else {
                    split1 = o1.getRouters()[1].getId().split("-");
                    split2 = o2.getRouters()[1].getId().split("-");
                    left = Integer.parseInt(split1[1]);
                    right = Integer.parseInt(split2[1]);

                    if (left < right) {
                        return -1;
                    } else if (left > right) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        });

        assertEquals(list.get(0).getRouters()[0].getId(), routers[0].getId());
        assertEquals(list.get(0).getRouters()[1].getId(), routers[2].getId());
        assertEquals(list.get(1).getRouters()[0].getId(), routers[1].getId());
        assertEquals(list.get(1).getRouters()[1].getId(), routers[3].getId());
        assertEquals(list.get(2).getRouters()[0].getId(), routers[2].getId());
        assertEquals(list.get(2).getRouters()[1].getId(), routers[3].getId());
    }

    @Test
    public void testRoutePlanWithBranch() {
        LinkInfo[] test = new LinkInfo[6];
        RouterInfo[] routers = createRouters(4);

        test[0] = new LinkInfo(1, new RouterInfo[] { routers[0], routers[2] });
        test[1] = new LinkInfo(2, new RouterInfo[] { routers[0], routers[3] });
        test[2] = new LinkInfo(3, new RouterInfo[] { routers[1], routers[0] });
        test[3] = new LinkInfo(2, new RouterInfo[] { routers[1], routers[3] });
        test[4] = new LinkInfo(1, new RouterInfo[] { routers[2], routers[3] });
        test[5] = new LinkInfo(4, new RouterInfo[] { routers[2], routers[1] });

        PrimMst stg = new PrimMst(test);
        LinkInfo[] mst = stg.execute();

        RoutingPlan plan = new RoutingPlan(mst, routers[2]);
        LinkInfo[] i = plan.getImmediates();
        assertEquals(i.length, 2);
        assertEquals(i[0].getRouters()[1].getId(), routers[3].getId());
        assertEquals(i[1].getRouters()[1].getId(), routers[0].getId());

        LinkInfo[] bplan = plan.getBranchPlan(i[0]);
        assertEquals(bplan.length, 1);
        bplan = plan.getBranchPlan(i[1]);
        assertEquals(bplan.length, 0);
    }

    @Test
    public void testRoutePlanWithoutBranch() {
        LinkInfo[] test = new LinkInfo[6];
        RouterInfo[] routers = createRouters(4);

        test[0] = new LinkInfo(1, new RouterInfo[] { routers[0], routers[2] });
        test[1] = new LinkInfo(2, new RouterInfo[] { routers[0], routers[3] });
        test[2] = new LinkInfo(3, new RouterInfo[] { routers[1], routers[0] });
        test[3] = new LinkInfo(2, new RouterInfo[] { routers[1], routers[3] });
        test[4] = new LinkInfo(1, new RouterInfo[] { routers[2], routers[3] });
        test[5] = new LinkInfo(4, new RouterInfo[] { routers[2], routers[1] });

        PrimMst stg = new PrimMst(test);
        LinkInfo[] mst = stg.execute();

        RoutingPlan plan = new RoutingPlan(mst, routers[1]);
        LinkInfo[] i = plan.getImmediates();
        assertEquals(i.length, 1);
        assertEquals(i[0].getRouters()[1].getId(), routers[3].getId());

        LinkInfo[] bplan = plan.getBranchPlan(i[0]);
        assertEquals(bplan.length, 2);
    }

    private RouterInfo[] createRouters(int count) {
        RouterInfo[] routers = new RouterInfo[count];
        for (int i = 0; i < count; i++) {
            routers[i] = new RouterInfo("id-" + i, "127.0.0." + i, 8000 + i);
        }

        return routers;
    }
}
