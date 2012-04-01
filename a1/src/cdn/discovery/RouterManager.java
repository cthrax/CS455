package cdn.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import cdn.shared.GlobalLogger;
import cdn.shared.MessagePrinter;
import cdn.shared.message.IMessage.MessageType;
import cdn.shared.message.IMessage.StatusCode;
import cdn.shared.message.IMessageRouter;
import cdn.shared.message.MessageWriter;
import cdn.shared.message.types.DeregisterRequestMessage;
import cdn.shared.message.types.DeregisterResponseMessage;
import cdn.shared.message.types.LinkInfo;
import cdn.shared.message.types.LinkWeightsUpdateMessage;
import cdn.shared.message.types.PeerRouterListMessage;
import cdn.shared.message.types.RegisterRequestMessage;
import cdn.shared.message.types.RegisterResponseMessage;
import cdn.shared.message.types.RouterInfo;

public class RouterManager implements IMessageRouter {
    private final Map<String, RouterInfo> routers = new HashMap<String, RouterInfo>();
    private CdnManager cdnManager;

    /**
     * Registers a new router in the database.
     *
     * @param client the socket associated with the connection.
     */
    public boolean registerNewRouter(Socket client, RegisterRequestMessage message) {
        boolean ret = false;
        try {
            RegisterResponseMessage response;
            if (isMatchingConnection(client, message.getInfo().getHostname()) && !isRegisteredRouter(message.getInfo().getId())) {
                routers.put(message.getInfo().getId(), message.getInfo());
                String successString = "Successfully registered with CDN. At present there are " + routers.size() + " total routers.";
                response = new RegisterResponseMessage(StatusCode.SUCCESS, successString);
                ret = true;
            } else {
                String failureMessage;
                if (isRegisteredRouter(message.getInfo().getId())) {
                    failureMessage = "This router is already registered with the CDN.";
                } else {
                    failureMessage = "An invalid packet was received.";
                }

                response = new RegisterResponseMessage(StatusCode.FAILURE, failureMessage);
            }

            new MessageWriter(client, response.getWireFormat()).write();
            client.close();
        } catch (IOException e) {
            ret = false;
            GlobalLogger.warning(this, "Failed to close socket and or write return message.");
        }
        return ret;
    }

    /**
     * Deregisters a router from the list.
     *
     * @param client the socket associated with this connection.
     */
    public boolean deregisterRouter(Socket client, DeregisterRequestMessage message) {
        boolean ret = false;
        try {
            DeregisterResponseMessage response;
            System.out.println("Deregistering.");
            if (isMatchingConnection(client, message.getInfo().getHostname()) && isRegisteredRouter(message.getInfo().getId())) {
                String successString = "Successfully deregistered with CDN.";
                response = new DeregisterResponseMessage(StatusCode.SUCCESS, successString);
                routers.remove(message.getInfo().getId());
                ret = true;
            } else {
                String failureMessage;
                if (!isRegisteredRouter(message.getInfo().getId())) {
                    failureMessage = "This router is not registered with the CDN.";
                } else {
                    failureMessage = "An invalid packet was received.";
                }

                response = new DeregisterResponseMessage(StatusCode.FAILURE, failureMessage);
            }

            System.out.println("DeregisterResponse: " + response.additionalInfo);
            new MessageWriter(client, response.getWireFormat()).write();
            client.close();
        } catch (IOException e) {
            GlobalLogger.warning(this, "Failed to close socket and or write return message.");
            ret = false;
        }
        return ret;
    }

    @Override
    public void handleMessage(SocketChannel client, byte[] buf, MessageType type) {
        switch (type) {
            case REGISTER_REQUEST:
                RegisterRequestMessage msg = new RegisterRequestMessage(buf);
                if (registerNewRouter(client.socket(), msg)) {
                    System.out.println("Successfully registered router " + msg.getInfo().getId() + ".");
                } else {
                    System.out.println("Failed to register router " + msg.getInfo().getId() + ".");
                }
                break;
            case DEREGISTER_REQUEST:
                DeregisterRequestMessage msg2 = new DeregisterRequestMessage(buf);
                if (deregisterRouter(client.socket(), msg2)) {
                    System.out.println("Successfully deregistered router " + msg2.getInfo().getId() + ".");
                } else {
                    System.out.println("Failed to deregister router " + msg2.getInfo().getId() + ".");
                }
                break;
            default:
                GlobalLogger.info(this, "Unknown message received.");
                break;
        }
    }

    private boolean isRegisteredRouter(String id) {
        return routers.containsKey(id);
    }

    private boolean isMatchingConnection(Socket client, String hostname) {
        InetAddress addr;
        try {
            GlobalLogger.debug(this, "Checking router with host: " + hostname);
            addr = InetAddress.getByName(hostname);
            return addr.equals(client.getInetAddress());
        } catch (UnknownHostException e) {
            GlobalLogger.warning(this, "Unable to find router host.");
        }
        return false;
    }

    public boolean isSetup() {
        return cdnManager != null && cdnManager.getCdn() != null && cdnManager.getCdn().getCurrentGraph() != null;
    }

    public LinkInfo[] getEdges() {
        return cdnManager.getCdn().getCurrentGraph();
    }

    public RouterInfo[] getRouters() {
        return routers.values().toArray(new RouterInfo[0]);
    }

    /**
     * Broadcasts appropriately to known routers the peer list.
     */
    public void advertisePeerList(int numberOfPeers) {
        Cdn cdn;
        if (cdnManager == null) {
            cdnManager = new CdnManager(routers.values().toArray(new RouterInfo[0]));
            cdn = cdnManager.createPeerList(numberOfPeers);
        } else {
            cdn = cdnManager.getCdn();
        }

        PeerList list = cdn.getNextAdvertisement();
        while (list != null) {
            Map<String, Peer> unnotifiedRouters = list.getUnnotifiedPeers();
            RouterInfo[] routerList = new RouterInfo[list.getSize()];
            RouterInfo parent = list.getParent().getInfo();
            list.getParent().setNotified(list.getParent());
            int count = 0;
            for (String key : unnotifiedRouters.keySet()) {
                unnotifiedRouters.get(key).setNotified(list.getParent());
                routerList[count++] = unnotifiedRouters.get(key).getInfo();
            }

            Socket client;
            try {
                client = new Socket(parent.getHostname(), parent.getPort());
                PeerRouterListMessage msg = new PeerRouterListMessage(routerList);
                new MessageWriter(client, msg.getWireFormat()).write();
                client.close();
            } catch (UnknownHostException e) {
                GlobalLogger.severe(this, "Failed to send PeerRouterList to router " + parent.getId() + " at " + parent.getHostname() + ":" + parent.getPort());
            } catch (IOException e) {
                GlobalLogger.severe(this,
                        "Problems sending PeerRouterList to router " + parent.getId() + " at " + parent.getHostname() + ":" + parent.getPort() + ": " + e.getMessage());
            }
            list = cdn.getNextAdvertisement();
        }

        cdn.resetNotifications();
    }

    /**
     * Broadcasts to all known routers the new edge weights.
     */
    public void advertiseLinkWeights() {
        if (cdnManager == null) {
            GlobalLogger.debug(this, "Cdn has not yet been setup, ignoring.");
            return;
        }

        LinkInfo[] edges = cdnManager.generateEdges();

        for (String key : routers.keySet()) {
            RouterInfo cur = routers.get(key);
            System.out.println("Advertising to " + MessagePrinter.print(cur));
            Socket client;
            try {
                client = new Socket(cur.getHostname(), cur.getPort());
                LinkWeightsUpdateMessage msg = new LinkWeightsUpdateMessage(edges);
                new MessageWriter(client, msg.getWireFormat()).write();
                client.close();
            } catch (UnknownHostException e) {
                GlobalLogger.severe(this, "Failed to send LinkWeightUpdate to router " + cur.getId() + " at " + cur.getHostname() + ":" + cur.getPort());
            } catch (IOException e) {
                GlobalLogger.severe(this, "Problems sending LinkWeightUpdate to router " + cur.getId() + " at " + cur.getHostname() + ":" + cur.getPort() + ": " + e.getMessage());
            }
        }
    }
}
