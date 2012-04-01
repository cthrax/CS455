package cdn.router;

import cdn.shared.message.types.RouterInfo;

public class Node {
    boolean isSelected = false;
    private final RouterInfo router;

    public Node(RouterInfo router) {
        this.router = router;
    }

    public RouterInfo getRouter() {
        return router;
    }

    public void setSelected() {
        isSelected = true;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
