package cdn.shared.message.types;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import cdn.discovery.Peer;
import cdn.discovery.PeerList;

public class TestPeers {
    @Test
    public void testNotification() {
        int numberOfRouters = 4;
        RouterInfo[] routers = new RouterInfo[numberOfRouters];
        for (int i = 0; i < routers.length; i++) {
            routers[i] = new RouterInfo("id-" + i, "127.0.0." + i, 8000 + i);
        }

        Peer peer = new Peer(routers[0]);
        Peer parent = new Peer(routers[1]);
        assertTrue(!peer.isNotified(parent), "Peer has been notified for parent?");

        peer.setNotified(parent);
        Peer parent2 = new Peer(routers[2]);
        assertTrue(peer.isNotified(parent), "Peer should have been notified.");
        assertTrue(!peer.isNotified(parent2), "Peer has been notified for parent2?");
    }

    @Test
    public void testNotificationWithPeerList() {
        int numberOfRouters = 4;
        RouterInfo[] routers = new RouterInfo[numberOfRouters];
        for (int i = 0; i < routers.length; i++) {
            routers[i] = new RouterInfo("id-" + i, "127.0.0." + i, 8000 + i);
        }


        Peer peer = new Peer(routers[0]);
        Peer parent = new Peer(routers[1]);
        Peer peer2 = new Peer(routers[2]);

        PeerList peerList1 = new PeerList(2, peer);
        peerList1.addPeer(parent);
        peerList1.addPeer(peer2);

        PeerList peerList2 = new PeerList(2, parent);
        peerList2.addPeer(peer);
        peerList2.addPeer(peer2);

        assertEquals(peerList1.getUnnotifiedPeers().size(), 2);

        peerList1.getParent().setNotified(peerList1.getParent());
        peerList1.getUnnotifiedPeers().get(parent.getInfo().getId()).setNotified(peerList1.getParent());
        peerList1.getUnnotifiedPeers().get(peer2.getInfo().getId()).setNotified(peerList1.getParent());
        assertEquals(peerList2.getUnnotifiedPeers().size(), 1);
    }
}
