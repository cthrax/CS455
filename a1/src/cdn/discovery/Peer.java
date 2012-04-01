package cdn.discovery;

import java.util.HashMap;
import java.util.Map;

import cdn.shared.message.types.RouterInfo;

public class Peer {
    private final RouterInfo peer;
    private final Map<String, Boolean> isNotified = new HashMap<String, Boolean>();

    public Peer(RouterInfo peer) {
        this.peer = peer;
    }

    public RouterInfo getInfo() {
        return peer;
    }

    public boolean isNotified(Peer parent) {
        String id = parent.getInfo().getId();
        if (isNotified.get(id) == null) {
            isNotified.put(id, false);
            return false;
        } else {
            return isNotified.get(id);
        }
    }

    public void setNotified(Peer parent) {
        String id = parent.getInfo().getId();
        isNotified.put(id, true);

        if (!parent.getInfo().getId().equals(peer.getId()) && !parent.isNotified(this)) {
            parent.setNotified(this);
        }
    }

    public void resetNotifications() {
        isNotified.clear();
    }
}
