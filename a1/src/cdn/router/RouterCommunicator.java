package cdn.router;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cdn.shared.GlobalLogger;
import cdn.shared.MessageException;
import cdn.shared.MessagePrinter;
import cdn.shared.message.IMessage.MessageType;
import cdn.shared.message.IMessage.StatusCode;
import cdn.shared.message.IMessageRouter;
import cdn.shared.message.MessageReader;
import cdn.shared.message.MessageWriter;
import cdn.shared.message.types.DeregisterRequestMessage;
import cdn.shared.message.types.DeregisterResponseMessage;
import cdn.shared.message.types.LinkInfo;
import cdn.shared.message.types.LinkWeightsUpdateMessage;
import cdn.shared.message.types.PeerRouterListMessage;
import cdn.shared.message.types.RegisterRequestMessage;
import cdn.shared.message.types.RegisterResponseMessage;
import cdn.shared.message.types.RouterConnectionMessage;
import cdn.shared.message.types.RouterDataMessage;
import cdn.shared.message.types.RouterInfo;

/**
 * This is responsible for sending out messages across the CDN.
 *
 * @author myles
 *
 */
public class RouterCommunicator implements IMessageRouter {

    private final InetAddress discoveryHost;
    private final int discoveryPort;
    private final List<RouterInfo> peers = new ArrayList<RouterInfo>();
    private final Map<String, SocketChannel> peerConnections = new HashMap<String, SocketChannel>();
    private RoutingPlan routePlan;
    private final RouterInfo self;
    private final Selector selector;
    private int tracker = 0;

    /**
     * The constructor.
     *
     * @param discoveryHost the host of the discovery node.
     * @param discoveryPort the port of the discovery node.
     */
    public RouterCommunicator(InetAddress discoveryHost, int discoveryPort, RouterInfo self, Selector selector) throws MessageException {
        this.discoveryHost = discoveryHost;
        this.discoveryPort = discoveryPort;
        this.self = self;
        this.selector = selector;

        try {
            registerWithDiscovery();
        } catch (MessageException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see cdn.shared.message.IMessageRouter#handleMessage(java.nio.channels.SocketChannel, byte[], cdn.shared.message.IMessage.MessageType)
     */
    @Override
    public void handleMessage(SocketChannel clientConnection, byte[] message, MessageType type) {
        switch (type) {
            case ROUTER_CONNECTION:
                try {
                    if (clientConnection.isBlocking()) {
                        clientConnection.configureBlocking(false);
                    }

                    if (!clientConnection.isRegistered()) {
                        clientConnection.register(selector, SelectionKey.OP_READ);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    GlobalLogger.severe(this, "Unable to make non-blocking peer connection in handleMessage.");
                }
                addPeerRouter(clientConnection, new RouterConnectionMessage(message).getRouter());
                break;
            case PASSING_DATA:
                RouterDataMessage msg = new RouterDataMessage(message);
                System.out.println("Received " + msg.getTracker() + " from " + msg.getRoot().getRouters()[0].getId());
                receiveDataMessage(msg);

                try {
                    clientConnection.register(selector, SelectionKey.OP_READ);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    GlobalLogger.severe(this, "Failed to re-register for read in handleMessage.");
                }
                break;
            case PEER_ROUTER_LIST:
                updatePeerRouters(clientConnection.socket(), new PeerRouterListMessage(message));
                break;
            case LINK_WEIGHT_UPDATE:
                // TODO: Do we need to notify discovery node this is received?
                createRoutePlan(new LinkWeightsUpdateMessage(message));
                System.out.println("MST calculated, ready to send data.");
                break;
            default:
                break;

        }

        if (clientConnection.isOpen()) {
            try {
                clientConnection.configureBlocking(false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * A getter for the RoutingPlan.
     *
     * @return the RoutingPlan currently setup.
     */
    public RoutingPlan getRouterPlan() {
        return routePlan;
    }

    /**
     * Deregister from the CDN.
     *
     * @throws MessageException on error.
     */
    public void exitCdn() throws MessageException {
        try {
            SocketChannel socket = SocketChannel.open();
            socket.configureBlocking(true);
            socket.connect(new InetSocketAddress(discoveryHost, discoveryPort));
            DeregisterRequestMessage message = new DeregisterRequestMessage(self);

            new MessageWriter(socket.socket(), message.getWireFormat()).write();
            MessageReader reader = MessageReader.fromSocketChannel(socket);
            MessageType type = reader.getType();
            if (type != MessageType.DEREGISTER_RESPONSE) {
                GlobalLogger.severe(this, "Invalid response received from server.");
                throw new MessageException("Invalid discoveryNode response. (1)");
            } else {
                byte[] bytes = new byte[reader.getSize()];
                reader.readRemains(bytes);

                DeregisterResponseMessage response = new DeregisterResponseMessage(bytes);

                if (response.status == StatusCode.FAILURE) {
                    throw new MessageException("Failed to deregister with discovery node: " + response.additionalInfo);
                } else if (response.status == StatusCode.SUCCESS) {
                    System.out.println(response.additionalInfo);

                    for (String key : peerConnections.keySet()) {
                        try {
                            peerConnections.get(key).close();
                        } catch (IOException ex) {
                            GlobalLogger.warning(this, "Failed to close peer connection, may have already been closed.");
                        }
                    }
                } else {
                    throw new MessageException("Invalid discoveryNode response. (2)");
                }
            }
        } catch (IOException e) {
            GlobalLogger.severe(this, e.getMessage());
            throw new MessageException("Failed to connect to discoveryNode due to unknown IO exception.");
        }
    }

    /**
     * Receive data from a peer router.
     *
     * @param message the message that was received.
     */
    private void receiveDataMessage(RouterDataMessage message) {
        tracker++;
        RoutingPlan plan = new RoutingPlan(message.getBranch(), self);
        // TODO: Is this right, or do we need to increment here?
        sendData(plan, message.getTracker());
    }

    /**
     * Sends a register request to the discovery node.
     *
     * @throws MessageException on errors.
     */
    private void registerWithDiscovery() throws MessageException {
        try {
            SocketChannel socket = SocketChannel.open();
            socket.configureBlocking(true);
            socket.connect(new InetSocketAddress(discoveryHost, discoveryPort));
            RegisterRequestMessage message = new RegisterRequestMessage(self);

            new MessageWriter(socket.socket(), message.getWireFormat()).write();
            MessageReader reader = MessageReader.fromSocketChannel(socket);
            MessageType type = reader.getType();
            if (type != MessageType.REGISTER_RESPONSE) {
                GlobalLogger.severe(this, "Invalid response received from server.");
                throw new MessageException("Invalid discoveryNode response. (1)");
            } else {
                byte[] bytes = new byte[reader.getSize()];
                reader.readRemains(bytes);

                RegisterResponseMessage response = new RegisterResponseMessage(bytes);

                if (response.status == StatusCode.FAILURE) {
                    throw new MessageException("Failed to register with discovery node: " + response.additionalInfo);
                } else if (response.status == StatusCode.SUCCESS) {
                    System.out.println(response.additionalInfo);
                } else {
                    throw new MessageException("Invalid discoveryNode response. (2)");
                }
            }
        } catch (IOException e) {
            GlobalLogger.severe(this, e.getMessage());
            throw new MessageException("Failed to connect to discoveryNode due to unknown IO exception.");
        }
    }

    /**
     * Initiate sending of data from this node, rather than forwarding on.
     */
    public void sendData() {
        sendData(routePlan, tracker);
        tracker++;
    }

    /**
     * Sends data across the CDN.
     *
     * @param plan the plan to use for sending this data.
     * @param tracker the tracker variable received.
     */
    private void sendData(RoutingPlan plan, int tracker) {
        LinkInfo[] immediates = plan.getImmediates();
        for (int i = 0; i < immediates.length; i++) {
            LinkInfo cur = immediates[i];
            LinkInfo[] branch = plan.getBranchPlan(cur);
            RouterDataMessage msg = new RouterDataMessage(cur, branch, tracker);

            try {
                SocketChannel conn = peerConnections.get(cur.getRouters()[1].getId());
                new MessageWriter(conn.socket(), msg.getWireFormat()).write();
                conn.register(selector, SelectionKey.OP_READ);

            } catch (IOException e) {
                GlobalLogger.severe(this, "Unable to send data message along edge " + MessagePrinter.print(cur));
                GlobalLogger.severe(this, "Reason: " + e.getMessage());
            }
        }
    }

    /**
     * Create a new router plan based on the linkWeightsUpdateMessage
     *
     * @param message the message that was received from the discovery node.
     */
    private void createRoutePlan(LinkWeightsUpdateMessage message) {
        IMST mstGenerator = new PrimMst(message.getLinks());
        routePlan = new RoutingPlan(mstGenerator.execute(), self);
    }

    /**
     * Add a peer connection to this router.
     *
     * @param peer the peer to add.
     * @param router the information about the router.
     */
    private void addPeerRouter(SocketChannel peer, RouterInfo router) {
        // Check it's a valid request
        if (isMatchingConnection(peer.socket(), router.getHostname()) && isNotConnected(router)) {
            peers.add(router);
            peerConnections.put(router.getId(), peer);
        } else {
            if (!isNotConnected(router)) {
                GlobalLogger.warning(this, "Received connection request for connected router.");
            } else {
                GlobalLogger.warning(this, "Received invalid packet, connection source must match router.");
            }
        }
    }

    /**
     * Receives the peer router list from the discovery node
     *
     * @param discoveryNode the discoveryNode connection in case future iterations would like to confirm receipt.
     * @param message the message that was sent by the discovery node.
     */
    private void updatePeerRouters(Socket discoveryNode, PeerRouterListMessage message) {
        RouterInfo[] newPeers = message.getRouters();
        for (int i = 0; i < newPeers.length; i++) {
            SocketChannel conn = null;
            try {
                conn = connectRouter(newPeers[i]);

                while (!conn.finishConnect()) {
                    Thread.sleep(10);
                }

                if (conn.finishConnect()) {
                    addPeerRouter(conn, newPeers[i]);
                    new MessageWriter(conn.socket(), new RouterConnectionMessage(self).getWireFormat()).write();
                    conn.register(selector, SelectionKey.OP_READ);
                } else {
                    GlobalLogger.warning(this, "Couldn't finish connecting.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                GlobalLogger.warning(this, "Failed to connect to router " + newPeers[i].getId() + ". CDN will be incomplete.");

                if (conn != null) {
                    try {
                        conn.close();
                    } catch (IOException e) {
                        GlobalLogger.debug(this, "Failed to close peer socket that was unsuccessful.");
                    }
                }
            }

        }
    }

    /**
     * Make a connection to a peer router and return the newly created connection.
     *
     * @param peer the peer that needs to be connected to.
     * @return the new SocketChannel.
     * @throws Exception can throw any exception, we want to handle that all above this method.
     */
    private SocketChannel connectRouter(RouterInfo peer) throws Exception {
        SocketChannel sock = null;
        try {
            sock = SocketChannel.open();
            sock.configureBlocking(false);
            sock.socket().setKeepAlive(true);
            sock.connect(new InetSocketAddress(peer.getHostname(), peer.getPort()));
        } catch (Exception ex) {
            throw ex;
        }
        return sock;
    }

    /**
     * Utility method for verifying same peer is who they say the are.
     *
     * @param client the Socket trying to connect.
     * @param hostname the name of the router from the message.
     * @return true if connection matches message, false otherwise.
     */
    private boolean isMatchingConnection(Socket client, String hostname) {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(hostname);
            return addr.getHostAddress().equals(client.getInetAddress().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Is peer already connected to this router.
     *
     * @param router the router that is trying to connect.
     * @return true if not connected, false otherwise.
     */
    private boolean isNotConnected(RouterInfo router) {
        return peerConnections.get(router.getId()) == null;
    }
}
