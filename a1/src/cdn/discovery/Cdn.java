package cdn.discovery;

import java.util.HashMap;

import cdn.shared.message.types.LinkInfo;

public class Cdn extends HashMap<String, PeerList> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private LinkInfo[] links;

    /**
     * Returns null if no node left to advertise.
     *
     * @return the PeerList to advertise.
     */
    public PeerList getNextAdvertisement() {
        for (String key : keySet()) {
            PeerList cur = get(key);
            if (cur.hasPeers()) {
                return cur;
            }
        }

        return null;
    }

    public void resetNotifications() {
        for (String key : keySet()) {
            PeerList cur = get(key);
            cur.resetNotifications();
        }
    }

    public LinkInfo[] getCurrentGraph() {
        return links;
    }

    public void setLinks(LinkInfo[] links) {
        this.links = links;
    }
}
