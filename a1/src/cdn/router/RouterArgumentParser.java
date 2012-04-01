package cdn.router;

import java.net.InetAddress;
import java.net.UnknownHostException;

import cdn.shared.GlobalLogger;

public class RouterArgumentParser {

    private final String[] args;
    private int portNum;
    private String id;
    private InetAddress discoveryHost;
    private int discoveryPort;

    public RouterArgumentParser(String[] args) {
        this.args = args;
    }

    /**
     * Determins if passed in arguments are valid for the Discovery Node.
     *
     * @return true if valid, false otherwise.
     */
    public boolean isValid() {
        boolean isValid = true;
        // Do some sanity checks on the arguments
        if (args.length != 4) {
            GlobalLogger.severe(this, "Invalid arguments.");
            return false;
        }
        int portNum = getPositiveNumber(args[0]);
        if (portNum < 0) {
            GlobalLogger.severe(this, "Invalid port number.");
            return false;
        } else if (portNum <= 1024) {
            GlobalLogger.warning(this, "You are running in the restricted port range, this is not recommended.");
        }

        this.portNum = portNum;

        id = args[1];

        if (id == null || id.equals("")) {
            GlobalLogger.severe(this, "Invalid id provided.");
            return false;
        }

        discoveryHost = getHostName(args[2]);

        if (discoveryHost == null) {
            GlobalLogger.severe(this, "Invalid discovery node hostname.");
            return false;
        }

        discoveryPort = getPositiveNumber(args[3]);
        if (discoveryPort < 0) {
            GlobalLogger.warning(this, "Invalid port for discovery node.");
            return false;
        }

        return isValid;
    }

    public InetAddress getDiscoveryHost() {
        return discoveryHost;
    }

    public String getId() {
        return id;
    }

    public int getDiscoveryPort() {
        return discoveryPort;
    }

    public int getPortNum() {
        return portNum;
    }

    /**
     * Utility method for getting a positive int from a String.
     *
     * @param num the string to parse.
     * @return a negative integer on invalid string, or the parsed positive int.
     */
    private int getPositiveNumber(String num) {
        int portNum = -1;
        try {
            portNum = Integer.parseInt(num);

            if (portNum < 0) {
                GlobalLogger.warning(this, "Number must be a positive number");
            }

        } catch (NumberFormatException e) {
            GlobalLogger.warning(this, "Argument must be a valid integer.");
        }

        return portNum;
    }

    private InetAddress getHostName(String args) {
        InetAddress ret = null;
        try {
            ret = InetAddress.getByName(args);
        } catch (UnknownHostException e) {
            GlobalLogger.severe(this, "Invalid hostname provided for discovery server.");
        }
        return ret;
    }
}
