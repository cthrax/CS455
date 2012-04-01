package cdn.shared.message;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Map;

import org.testng.annotations.Test;

import cdn.discovery.Cdn;
import cdn.discovery.CdnManager;
import cdn.discovery.Peer;
import cdn.discovery.PeerList;
import cdn.shared.message.types.RouterInfo;

public class TestCdnManager {

    @Test
    public void testCdnCreation() {
        validateCdnCreation(8, 3);
        validateCdnCreation(10, 4);
        validateCdnCreation(4, 3);
        validateCdnCreation(4, 2);
        validateCdnCreation(2, 1);
    }

    @Test
    public void testPeerRouterSending() {
        int numberOfRouters = 4;
        int connCount = 3;
        RouterInfo[] routers = new RouterInfo[numberOfRouters];
        for (int i = 0; i < routers.length; i++) {
            routers[i] = new RouterInfo("id-" + i, "127.0.0." + i, 8000 + i);
        }

        CdnManager cdnManager = new CdnManager(routers);
        Cdn cdn = cdnManager.createPeerList(connCount);

        PeerList list = cdn.getNextAdvertisement();
        for (int i = 0; i < 3; i++) {
            Map<String, Peer> unnotifiedRouters = list.getUnnotifiedPeers();
            assertEquals(unnotifiedRouters.size(), connCount - i);

            RouterInfo[] routerList = new RouterInfo[list.getSize()];
            list.getParent().setNotified(list.getParent());
            int count = 0;
            for (String key : unnotifiedRouters.keySet()) {
                unnotifiedRouters.get(key).setNotified(list.getParent());
                routerList[count++] = unnotifiedRouters.get(key).getInfo();
            }
            list = cdn.getNextAdvertisement();
        }
        assertNull(cdn.getNextAdvertisement(), "Last node should already be notified by other nodes!");
    }

    private void validateCdnCreation(int numberOfRouters, int connCount) {
        RouterInfo[] routers = new RouterInfo[numberOfRouters];
        for (int i = 0; i < routers.length; i++) {
            routers[i] = new RouterInfo("id-" + i, "127.0.0." + i, 8000 + i);
        }

        CdnManager cdnManager = new CdnManager(routers);
        Cdn cdn = cdnManager.createPeerList(connCount);

        System.out.println("CDN:");
        for (String key : cdn.keySet()) {
            PeerList cur = cdn.get(key);
            System.out.println("Router " + key);

            Map<String, Peer> list = cur.getUnnotifiedPeers();
            for (String key2 : list.keySet()) {
                System.out.println("\tPeer: " + key2);
            }
        }

        for (String key : cdn.keySet()) {
            PeerList cur = cdn.get(key);
            assertEquals(cur.getSize(), connCount);

            Map<String, Peer> list = cur.getUnnotifiedPeers();
            for (String key2 : list.keySet()) {
                PeerList pl = cdn.get(list.get(key2).getInfo().getId());
                assertNotNull(pl.getUnnotifiedPeers().get(key), "No backwards link.");
            }
        }
    }
}
