package cs455.nfs.remote;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import cs455.nfs.client.InvalidDirException;
import cs455.nfs.client.LocalServiceModel;
import cs455.nfs.client.RemoteServiceModel;
import cs455.nfs.shared.structure.Filesystem;
import cs455.nfs.shared.structure.PathArrayVisitor;
import cs455.nfs.shared.structure.messages.AddFileMessage;
import cs455.nfs.shared.structure.messages.AddFolderMessage;
import cs455.nfs.shared.structure.messages.Failure;
import cs455.nfs.shared.structure.messages.IMessage;
import cs455.nfs.shared.structure.messages.MoveMessage;
import cs455.nfs.shared.structure.messages.PeekMessageRequest;
import cs455.nfs.shared.structure.messages.PeekMessageResponse;
import cs455.nfs.shared.structure.messages.RmMessage;
import cs455.nfs.shared.structure.messages.Success;
import cs455.nfs.shared.structure.node.INode;

public class DirectoryService {

    private final int port;

    private Filesystem local;

    public DirectoryService(final int port) {
        this.port = port;
    }

    public void run() {
        try {
            openDirectory();
            ServerSocket socket = new ServerSocket(port);
            while (true) {
                Socket client = socket.accept();
                ObjectInputStream s = new ObjectInputStream(client.getInputStream());
                try {
                    Object obj = s.readObject();
                    System.out.println("received command " + obj.getClass());
                    if (obj instanceof PeekMessageRequest) {
                        peek(client);
                    } else if (obj instanceof RmMessage) {
                        remove(client, (RmMessage) obj);
                    } else if (obj instanceof AddFolderMessage) {
                        add(client, (AddFolderMessage) obj);
                    } else if (obj instanceof MoveMessage) {
                        move(client, (MoveMessage) obj);
                    } else if (obj instanceof AddFileMessage) {
                        addFile(client, (AddFileMessage) obj);
                    }
                } catch (ClassNotFoundException e) {
                    writeMessage(client, new Failure(e.getMessage()));
                }

            }
        } catch (IOException e) {
            System.out.println("There was an error while communicating over the network: " + e.getMessage());
        }
    }

    private void addFile(final Socket client, final AddFileMessage msg) {
        File newFile = ((LocalServiceModel) local.getServiceModel()).getRealFile(msg.getFullPath());
        try {
            if (!newFile.createNewFile()) {
                writeMessage(client, new Failure("Failed to create new file."));
            }
            writeMessage(client, new Success(""));

            long total = msg.getSize();
            int bytesRead = 0;
            byte[] buf = new byte[1400];
            FileOutputStream s = new FileOutputStream(newFile);
            BufferedInputStream in = new BufferedInputStream(client.getInputStream());
            while ((bytesRead = in.read(buf, 0, 1400)) != -1) {
                s.write(buf, 0, 1400);
                s.flush();
            }

            writeMessage(client, new Success("Successfully moved file."));

        } catch (IOException e) {
            e.printStackTrace();
            writeMessage(client, new Failure(e.getMessage()));
        }
    }

    private void move(final Socket client, final MoveMessage obj) {
        cs455.nfs.shared.structure.messages.File src = obj.getSrc();
        cs455.nfs.shared.structure.messages.File dest = obj.getDest();

        if (dest.getHost().equals(src.getHost())) {
            try {
                System.out.println("Moving locally.");
                INode nsrc = local.findByPath(src.getPath());
                local.addPath(dest.getPath());
                INode ndst = local.findByPath(dest.getPath());

                local.getServiceModel().mvFile(nsrc, ndst);

                if (nsrc.getParent() != null) {
                    nsrc.getParent().removeChild(nsrc);
                }

                writeMessage(client, new Success(""));

            } catch (Exception e) {
                e.printStackTrace();
                writeMessage(client, new Failure(e.getMessage()));
            }
        } else {
            try {
                RemoteServiceModel rem = new RemoteServiceModel(dest.getHost(), dest.getPort());
                java.io.File f = ((LocalServiceModel) local.getServiceModel()).getRealFile(src.getPath());
                rem.addFile(dest.getPath(), f.length(), f);
                local.rmFile(local.findByPath(src.getPath()));
                writeMessage(client, new Success(""));
            } catch (InvalidDirException e) {
                writeMessage(client, new Failure(e.getMessage()));
            } catch (Exception e) {
                writeMessage(client, new Failure(e.getMessage()));
            }
        }
    }

    private void add(final Socket client, final AddFolderMessage obj) {
        try {
            local.addPath(obj.getFolderPath());
            writeMessage(client, new Success(""));
        } catch (Exception e) {
            writeMessage(client, new Failure(e.getMessage()));
        }
    }

    private void remove(final Socket client, final RmMessage obj) {
        try {
            local.rmDir(local.findByPath(obj.getPath()));
            writeMessage(client, new Success(""));
        } catch (InvalidDirException e) {
            writeMessage(client, new Failure(e.getMessage()));
        } catch (Exception e) {
            writeMessage(client, new Failure(e.getMessage()));
        }
    }

    private void peek(final Socket client) {
        PathArrayVisitor v = new PathArrayVisitor();
        v.visitDirectory(local.getRoot());
        writeMessage(client, new PeekMessageResponse(v.getPathArray()));
    }

    private void writeMessage(final Socket client, final IMessage message) {
        ObjectOutputStream s;
        try {
            s = new ObjectOutputStream(client.getOutputStream());
            s.writeObject(message);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void openDirectory() {
        File dir = new File("/tmp/bostwick/HW3");
        dir.mkdirs();
        List<String> l = new LinkedList<String>();
        addDirs(l, dir);
        local = new Filesystem(new LocalServiceModel(dir));
        local.addSerializedNodes(l.toArray(new String[0]));
    }

    private void addDirs(final List<String> files, final File root) {
        if (root.getName().equals(".") || root.getName().equals("..")) {
            return;
        }

        // This assumes an empty directory
        String path = root.getAbsolutePath().replace("/tmp/bostwick/HW3", "").replace("/", "/D");
        path = "D" + path;

        if (root.isDirectory() && root.list() != null && root.list().length == 0) {
            files.add(path);
            return;
        }

        if (!root.isDirectory() && root.exists()) {
            files.add(path.replace("D" + root.getName(), "F" + root.getName()));
        } else {
            for (File f : root.listFiles()) {
                addDirs(files, f);
            }
        }
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        DirectoryServiceArgumentParser parser = new DirectoryServiceArgumentParser(args);

        if (parser.isValid()) {
            DirectoryService service = new DirectoryService(parser.getPortNum());
            service.run();
        }
    }

}
