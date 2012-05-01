package cs455.nfs.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cs455.nfs.shared.structure.Filesystem;
import cs455.nfs.shared.structure.PathPrinterVisitor;
import cs455.nfs.shared.structure.node.INode;

public class ClientModule {

    // Enum for accepted commands
    private enum Command {
        cd("vcd"),
        mkdir("vmkdir"),
        mv("vmv"),
        rm("vrm"),
        ls("vls"),
        pwd("vpwd"),
        mount("vmount"),
        peek("peek"),
        exit("exit"),
        invalid("");

        private final String value;

        private Command(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Command parseByValue(final String value) {
            Command[] values = Command.values();
            for (int i = 0; i < values.length; i++) {
                Command cur = values[i];

                if (cur.getValue().equals(value)) {
                    return cur;
                }
            }

            return Command.invalid;
        }
    }

    private final Navigator nav = new Navigator();
    private boolean exit = false;

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (!exit) {
                String buf = reader.readLine();
                if (buf != null) {
                    String command = buf.toLowerCase();
                    String[] list = command.split(" ");
                    String[] rest = new String[list.length - 1];
                    for (int i = 1; i < list.length; i++) {
                        rest[i - 1] = list[i];
                    }
                    command = list[0];
                    handleCommand(Command.parseByValue(command), rest);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * All commands are handled from here.
     *
     * @param command the command to execute.
     * @param args the args that might be associated with this command.
     */
    private void handleCommand(final Command command, final String[] args) {
        switch (command) {
            case cd:
                cd(args);
                break;
            case rm:
                rm(args);
                break;
            case pwd:
                nav.printWorkingDirectory();
                break;
            case ls:
                nav.list();
                break;
            case mount:
                mount(args);
                break;
            case mkdir:
                makeDir(args);
                break;
            case mv:
                moveFile(args);
                break;
            case exit:
                exit = true;
                break;
            case peek:
                peek(args);
                break;
            default:
                System.out.println("Unknown command.");
                break;
        }
    }

    /**
     * Peek is responsible for fetching the directory structure from the passed in values for host and port.
     *
     * @param args the values host and port, in an ideal situation.
     */
    private void peek(final String[] args) {
        if (args.length != 2) {
            System.out.println("peek requires a host and port.");
        } else {
            try {
                RemoteServiceModel model = new RemoteServiceModel(args[0], Integer.parseInt(args[1]));

                // TODO: Error handling
                Filesystem fs = model.peek();
                if (fs == null) {
                    System.out.println("Unable to retrieve directory service listing.");
                    return;
                }

                PathPrinterVisitor v = new PathPrinterVisitor();
                v.visitDirectory(fs.getRoot());
                String[] list = v.getPathArray();
                for (int i = 0; i < list.length; i++) {
                    System.out.println(list[i]);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number entered.");
            }
        }
    }

    /**
     * Change Directory, this changes the current directory based on the value that is passed in. *Note, complex changes not supported, ie multi-dirs.
     *
     * @param args the directory to change to.
     */
    private void cd(final String[] args) {
        if (args.length != 1) {
            System.out.println("vcd only takes one argument.");
        } else {
            try {
                nav.changeDir(args[0]);
            } catch (InvalidDirException ex) {
                System.out.println(args[0] + " is not a valid dir to navigate to.");
            }
        }
    }

    /**
     * Removes a folder, if possible.
     *
     * @param args the folder to remove.
     */
    private void rm(final String[] args) {
        if (args.length != 1) {
            System.out.println("vrm only takes one argument.");
        } else {
            try {
                nav.rmDir(args[0]);
            } catch (InvalidDirException ex) {
                System.out.println(args[0] + " cannot be removed.");
            }
        }
    }

    /**
     * Moves a file from the specificed source to the specified destination, this can be a complex folder path.
     *
     * @param args the paths to move from and to.
     */
    private void moveFile(final String[] args) {
        if (args.length != 2) {
            System.out.println("vmv requires two arguments.");
        } else {
            try {
                nav.moveFile(args[0], args[1]);
            } catch (InvalidDirException e) {
                System.out.println("Failed to move file: " + e.getMessage());
            } catch (Exception e) {
                // e.printStackTrace();
                System.out.println("Failed to move file: " + e.getMessage());
            }
        }
    }

    /**
     * Mounts a remote directory service into the virtual filesystem.
     *
     * @param args the host, port and path to mount.
     */
    private void mount(final String[] args) {
        if (args.length != 3) {
            System.out.println("vmount requires 3 arguments");
        } else {
            try {
                String host = args[0];
                int port = Integer.parseInt(args[1]);
                RemoteServiceModel model = new RemoteServiceModel(host, port);
                // TODO: Error handling
                Filesystem fs = model.peek();
                if (fs == null) {
                    System.out.println("Failed to retrieve directory service listing.");
                }

                try {
                    INode node = fs.findByPath(args[2]);
                    if (node == null) {
                        System.out.println("No directory by that name found.");
                    }
                    nav.mountDir(node);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number passed in.");
            }
        }
    }

    /**
     * Make a directory at the current folder. Does not allow for multiple folders.
     *
     * @param args the name of the directory to make.
     */
    private void makeDir(final String[] args) {
        if (args.length != 1) {
            System.out.println("vmkdir only takes 1 argument");
        } else {
            try {
                nav.addDir(args[0]);
            } catch (InvalidDirException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        ClientModule client = new ClientModule();
        client.run();
    }

}
