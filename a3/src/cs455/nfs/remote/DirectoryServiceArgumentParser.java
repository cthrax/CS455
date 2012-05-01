package cs455.nfs.remote;


public class DirectoryServiceArgumentParser {

    private final String[] args;
    private int portNum;

    public DirectoryServiceArgumentParser(String[] args) {
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
        if (args.length != 1) {
            System.out.println("Invalid number of arguments.");
            return false;
        }
        int portNum = getPositiveNumber(args[0]);
        if (portNum < 0) {
            System.out.println("Invalid port number.");
            return false;
        } else if (portNum <= 1024) {
            System.out.println("You are running in the restricted port range, this is not recommended.");
        }

        this.portNum = portNum;

        return isValid;
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
                System.out.println("Port number must be positive.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Argument must be a valid integer.");
        }

        return portNum;
    }
}
