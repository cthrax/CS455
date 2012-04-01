package cdn.discovery;

import cdn.shared.GlobalLogger;

/**
 * Parses the arguments and does some sanity checks on them.
 *
 * @author myles
 *
 */
public class DiscoveryArgumentParser {
    private final String[] args;
    private int portNum;
    private int refreshInterval;

    public DiscoveryArgumentParser(String[] args) {
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
        int portNum = getPositiveNumber(args[0]);
        if (portNum < 0) {
            GlobalLogger.severe(this, "Invalid port number.");
            return false;
        } else if (portNum <= 1024) {
            GlobalLogger.warning(this, "You are running in the restricted port range, this is not recommended.");
        }

        this.portNum = portNum;

        if (args.length > 1) {
            int refreshInterval = getPositiveNumber(args[1]);
            if (refreshInterval < 0) {
                GlobalLogger.warning(this, "Invalid refresh interval.");
                return false;
            }
            this.refreshInterval = refreshInterval;
        } else {
            refreshInterval = 120;
        }

        return isValid;
    }

    public int getRefreshInterval() {
        return refreshInterval;
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
}
