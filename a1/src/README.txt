Myles Bostwick
828-763-363

#### GTA INFO ####
1. While testing on my system, I noticed considerable delays when opening up sockets (which means the disemination of the initial setup-cdn call can take several seconds to complete). There is a message to indicate the completion of setup.

2. It didn't say anywhere explicitly that the LinkWeightUpdateMessage should go out with the "setup-cdn" command, but I felt that made the most sense, so I did that.

3. Since there was no mention of a register-with-cdn command, my routers register with the CDN on coming up. The router protects against sending a deregister message twice. Once a successful message has been sent, no more may be sent.

4. The routers will fail really badly if an insufficiently sized Cr is passed into the setup-cdn command, namely if one is passed in for a router network of greater than 2, but the Discovery node, should prevent this type of input.

5. It's worth noting that, due to a misunderstanding on my part, I am using the java.nio package for handling connections. 

6. There was no indication in the assignment how robust the CDN was supposed to be, so if a connection is dropped for whatever reason (the router is shutdown or deregisters), a message notes that, and no communication happens over that link. This could have been setup to try and re-initiate a connection, but there was no explicit guidelines for that.

#### FILE DESCRIPTIONS #######
cdn/discovery/Cdn.java
    Represents the CDN itself, including all edges and weights.

cdn/discovery/CdnManager.java
    This is in charge of generating the CDN as well as regenerating weights along the existing edges when prompted by the discovery node.

cdn/discovery/DiscoveryArgumentParser.java
    This parses and validates arguments that are sent to the Discovery Node Main method.

cdn/discovery/Peer.java
    Represents a router's direct peer while creating the initial CDN.

cdn/discovery/PeerList.java
    Represents the list of peers for each node in the CDN.

cdn/discovery/RouterManager.java
    Control interface for sending/receiving data with Routers in the CDN.

cdn/Discovery.java
    The main entry point the discovery node. This also take care of parsing arguments and starting up the three worker threads; Message Listener, Message Sender and Command Listener.

cdn/router/Edge.java
    This represents an Edge while creating the MST.

cdn/router/IMST.java
    This is an interface for the MST generator. In case down the road a different algorithm were to be used, very little code would actually have to change.

cdn/router/Node.java
    This represents a Node within the graph while creating the MST.

cdn/router/PrimMst.java
    The actual implementation of the Prim MST algorithm.
cdn/router/RouterArgumentParser.java
    This parses and validates the arguments from the command line for starting up the Router.

cdn/router/RouterCommunicator.java
    This is the control interface for interactions with the Router from the CDN. Whether it is messages from other routers or the Discovery Node itself.

cdn/router/RoutingPlan.java
    This is a representation of the routing plan that has some convenience methods, like determining if this node has a branch, getting an easily serializable array and re-ordering edges so they become directed along the path desired.

cdn/Router.java
    This is the main entry point for a router node, it parses the command line arguments and starts up the three worker threads; Message Listener, Message Sender and Command Listener.

cdn/shared/CommandListener.java
    This is the Runnable class that listens for input from the user and queues the commands for action.

cdn/shared/GlobalLogger.java
    This is a convenience class created so that logging levels can be turned up and down through the system, greatly optimizing debuggability.

cdn/shared/ICommandExecutor.java
    This represents a unit of work received from the user, that needs to be done.

cdn/shared/ICommandRunner.java
    This represents an object that receives ICommandExecutor objects for execution.

cdn/shared/IQueuedMessage.java
    This represents a message that has been received and is in queue waiting for processing.

cdn/shared/IWorker.java
    This is an interface for a parallel worker thread, it has a method for cancelling workers.

cdn/shared/message/IMessage.java
    A generic interface for messages, as well as the host for the MessageType enum.

cdn/shared/message/IMessageElement.java
    An instance member of a message that is serializable, but does not represent a complete message that can be sent over the wire.

cdn/shared/message/IMessageRouter.java
    An interface for handling queued Messages.

cdn/shared/message/MessageListener.java
    The Runnable class responsible for listening for all incoming connections. This uses non-blocking IO to select from any number of peer routers or to handle incoming connections from the Discovery node.

cdn/shared/message/MessageReader.java
    This is responsible for centralizing the work necessary to read data from a SocketChannel.

cdn/shared/message/MessageSender.java
    This is a Runnable that writes out any responses needed to messages, as well as doing some very basic validation of the message received.

cdn/shared/message/MessageWriter.java
    This is responsible for centralizing the logic necessary for writing out on a SocketChannel.

cdn/shared/message/QueuedMessage.java
    This is the concrete class representing a received message that still needs processing.

cdn/shared/message/types/DeregisterRequestMessage.java
    Represents the DeregisterRequestMessage.

cdn/shared/message/types/DeregisterResponseMessage.java
    Represents the DeregisterResponseMessage.

cdn/shared/message/types/LinkInfo.java
    Represents the information around a link between two routers. This includes the two routers as well as a weight for that link.

cdn/shared/message/types/LinkWeightsUpdateMessage.java
    Represents the LinkWeightsUpdateMessage.

cdn/shared/message/types/PeerRouterListMessage.java
    Represents the PeerRouterListMessage.

cdn/shared/message/types/RegisterRequestMessage.java
    Represents the RegisterRequestMessage.

cdn/shared/message/types/RegisterResponseMessage.java
    Represents the RegisterResponseMessage.

cdn/shared/message/types/RouterConnectionMessage.java
    Represents a message of a router initiating a connection to a peer.

cdn/shared/message/types/RouterDataMessage.java
    Represents the message for sending data around the cdn.

cdn/shared/message/types/RouterInfo.java
    Represents a router, this includes id, hostname, and port.

cdn/shared/MessageException.java
    An exception for communicating issues while processing a message.

cdn/shared/MessagePrinter.java
    A class for printing out various messages or messages elements. Mostly for convenience, entirly static methods.

cdn/shared/QueueManager.java
    A generic class for creating a thread-safe queue that can be pass a type of queuedItems. This is what makes cross-thread communication possible.
