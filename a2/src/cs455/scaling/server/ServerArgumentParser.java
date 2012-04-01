package cs455.scaling.server;

import cs455.scaling.shared.GlobalLogger;

public class ServerArgumentParser {
    private final String[] args;
    private int portNum;
    private int threadPoolSize;

    public ServerArgumentParser(String[] args) {
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
        if (args.length != 2) {
            GlobalLogger.severe(this, "Invalid number of arguments, must specify both port number and thread pool size.");
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

        int threadPoolSize = getPositiveNumber(args[1]);
        if (threadPoolSize < 0) {
            GlobalLogger.warning(this, "Invalid refresh interval.");
            return false;
        }
        this.threadPoolSize = threadPoolSize;

        return isValid;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
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
