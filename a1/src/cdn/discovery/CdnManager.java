package cdn.discovery;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cdn.shared.message.types.LinkInfo;
import cdn.shared.message.types.RouterInfo;

/**
 * Responsible for actions related to the creation/manipulation of the CDN.
 * 
 * @author myles
 * 
 */
public class CdnManager {
    private final RouterInfo[] routers;
    private final Map<String, Peer> peers = new HashMap<String, Peer>();
    private Cdn cdn;
    private int numberOfPeers = 0;

    public CdnManager(RouterInfo[] routers) {
        this.routers = routers;

        for (int i = 0; i < routers.length; i++) {
            peers.put(routers[i].getId(), new Peer(routers[i]));
        }
    }

    public Cdn getCdn() {
        return cdn;
    }

    /**
     * This creates the initial graph for the CDN.
     * 
     * @param numberOfPeers the number of connections each router must have.
     * @return the newly created CDN.
     */
    public Cdn createPeerList(final int numberOfPeers) {
        this.numberOfPeers = numberOfPeers;
        int[] randomIndices = new int[routers.length];
        cdn = new Cdn();

        // Initialize with all indices.
        for (int i = 0; i < routers.length; i++) {
            randomIndices[i] = i;
        }

        // Create random list of indices so that these nodes can be strung
        // together with a guarantee of no partitions, but is still random
        // This "Fisher-Yates Shuffle"
        // http://en.wikipedia.org/wiki/Fisher-Yates_shuffle
        Random randomGenerator = new Random(new Date().getTime());
        for (int i = 0; i < routers.length; i++) {
            // Use random so we can seed with time and get less consistency from the pseudo-random generator.
            int j = randomGenerator.nextInt(routers.length);
            int swap = randomIndices[i];
            randomIndices[i] = randomIndices[j];
            randomIndices[j] = swap;
        }

        for (int i = 0; i < randomIndices.length; i++) {
            RouterInfo cur = routers[randomIndices[i]];

            // Check if current peerList exists already
            PeerList list = getPeerList(numberOfPeers, cur.getId());
            assignPeers(list, i, numberOfPeers, randomIndices);
        }

        return cdn;
    }

    public LinkInfo[] generateEdges() {
        // A regular graph has (nodes * degree / 2) edges
        int numberOfEdges = routers.length * numberOfPeers / 2;
        LinkInfo[] links = new LinkInfo[numberOfEdges];
        int count = 0;
        PeerList list = cdn.getNextAdvertisement();
        Random rGen = new Random(new Date().getTime());
        while (list != null) {
            Map<String, Peer> peers = list.getUnnotifiedPeers();
            for (String key : peers.keySet()) {
                Peer cur = peers.get(key);
                links[count++] = createEdge(rGen, list.getParent(), cur);
            }
            list = cdn.getNextAdvertisement();
        }
        cdn.resetNotifications();
        cdn.setLinks(links);
        return links;
    }

    private LinkInfo createEdge(Random rGen, Peer r1, Peer r2) {
        RouterInfo[] routers = new RouterInfo[2];
        routers[0] = r1.getInfo();
        routers[1] = r2.getInfo();
        r1.setNotified(r2);
        r2.setNotified(r1);
        return new LinkInfo(rGen.nextInt(9) + 1, routers);
    }

    private void assignPeers(PeerList cur, int curIdx, int numberOfPeers, int[] randomIndices) {
        boolean odd = numberOfPeers % 2 != 0;
        int half = numberOfPeers / 2;
        Peer curPeer = peers.get(cur.getParent().getInfo().getId());

        for (int i = 1; i <= half; i++) {

            // Right
            String rightId = getIdFromRouterList(randomIndices, getIndex(curIdx + i, randomIndices.length));
            PeerList right = getPeerList(numberOfPeers, rightId);

            // Left
            String leftId = getIdFromRouterList(randomIndices, getIndex(curIdx - i, randomIndices.length));
            PeerList left = getPeerList(numberOfPeers, leftId);

            if (cur.getSize() < numberOfPeers) {
                cur.addPeer(peers.get(rightId));
                right.addPeer(curPeer);
            }

            if (cur.getSize() < numberOfPeers) {
                cur.addPeer(peers.get(leftId));
                left.addPeer(curPeer);
            }
        }

        if (odd && numberOfPeers > 0 && cur.getSize() < numberOfPeers) {
            // Right
            String rightId = getIdFromRouterList(randomIndices, getIndex(curIdx + half + 1, randomIndices.length));
            PeerList right = getPeerList(numberOfPeers, rightId);
            cur.addPeer(peers.get(rightId));
            right.addPeer(curPeer);
        }
    }

    private int getIndex(int index, int max) {
        int ret = index;
        if (index < 0) {
            ret = max + index;
        } else if (index >= max) {
            ret = index % max;
        }

        return ret;
    }

    private String getIdFromRouterList(int[] randomIndices, int idx) {
        return routers[randomIndices[idx]].getId();
    }

    private PeerList getPeerList(int numberOfPeers, String peerId) {
        PeerList ret;
        if (cdn.get(peerId) == null) {
            ret = new PeerList(numberOfPeers, peers.get(peerId));
            cdn.put(peerId, ret);
        } else {
            ret = cdn.get(peerId);
        }

        return ret;
    }

}
