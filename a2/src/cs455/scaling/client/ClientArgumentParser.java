package cs455.scaling.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import cs455.scaling.shared.GlobalLogger;

public class ClientArgumentParser {

    private final String[] args;
    private int serverPortNum;
    private InetAddress serverHost;
    private int messageRate;

    public ClientArgumentParser(String[] args) {
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
        if (args.length != 3) {
            GlobalLogger.severe(this, "Invalid arguments, must specify all of server host, serer port and message rate.");
            return false;
        }

        serverHost = getHostName(args[0]);

        if (serverHost == null) {
            GlobalLogger.severe(this, "Invalid server node hostname.");
            return false;
        }

        int serverPortNum = getPositiveNumber(args[1]);
        if (serverPortNum < 0) {
            GlobalLogger.severe(this, "Invalid port number.");
            return false;
        }

        this.serverPortNum = serverPortNum;

        messageRate = getPositiveNumber(args[2]);
        if (messageRate < 0) {
            GlobalLogger.warning(this, "Invalid message rate.");
            return false;
        }

        return isValid;
    }

    public InetAddress getServerHost() {
        return serverHost;
    }

    public int getMessageRate() {
        return messageRate;
    }

    public int getServerPort() {
        return serverPortNum;
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
