package cdn.discovery;

import java.util.HashMap;
import java.util.Map;

public class PeerList {
    private final Map<String, Peer> peers = new HashMap<String, Peer>();
    private final Peer parent;
    private final int numberOfPeers;

    public PeerList(int numberOfPeers, Peer parent) {
        this.parent = parent;
        this.numberOfPeers = numberOfPeers;
    }

    public boolean addPeer(Peer peer) {
        if (peer.getInfo().getId().equals(parent.getInfo().getId()) || peers.containsKey(peer.getInfo().getId()) || numberOfPeers == peers.size()) {
            return false;
        } else {
            peers.put(peer.getInfo().getId(), peer);
            return true;
        }
    }

    public Peer getParent() {
        return parent;
    }

    public boolean hasPeers() {
        return getUnnotifiedPeers().size() > 0;
    }

    public int getSize() {
        return getUnnotifiedPeers().size();
    }

    /**
     * This method returns peers that have not already been notified via another peer.
     * 
     * @return the Map of unnotified peers.
     */
    public Map<String, Peer> getUnnotifiedPeers() {
        Map<String, Peer> list = new HashMap<String, Peer>();
        for (String key : peers.keySet()) {
            if (!peers.get(key).isNotified(parent)) {
                list.put(key, peers.get(key));
            }
        }

        return list;
    }

    public void resetNotifications() {
        parent.resetNotifications();
    }
}
