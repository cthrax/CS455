package cs455.nfs.client;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import cs455.nfs.shared.structure.Filesystem;
import cs455.nfs.shared.structure.messages.AddFileMessage;
import cs455.nfs.shared.structure.messages.AddFolderMessage;
import cs455.nfs.shared.structure.messages.Failure;
import cs455.nfs.shared.structure.messages.File;
import cs455.nfs.shared.structure.messages.IMessage;
import cs455.nfs.shared.structure.messages.MoveMessage;
import cs455.nfs.shared.structure.messages.PeekMessageRequest;
import cs455.nfs.shared.structure.messages.PeekMessageResponse;
import cs455.nfs.shared.structure.messages.RmMessage;
import cs455.nfs.shared.structure.messages.Success;
import cs455.nfs.shared.structure.node.INode;

public class RemoteServiceModel implements IServiceModel {
    private final String host;
    private final int port;

    public RemoteServiceModel(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void addDir(final INode node) {
        addDir(node.getFullPath());
    }

    public void addDir(final String path) {
        Object obj = sendReceive(new AddFolderMessage(path));
        if (obj instanceof Success) {
            System.out.println("Successfully added " + path);
        } else if (obj instanceof Failure) {
            System.out.println("Failed to add " + path + " because " + ((Failure) obj).getMessage());
        }
    }

    @Override
    public void rmDir(final INode node) {
        Object obj = sendReceive(new RmMessage(node.getFullPath()));
        if (obj instanceof Success) {
            System.out.println("Successfully removed " + node.getFullPath());
        } else if (obj instanceof Failure) {
            System.out.println("Failed to remove " + node.getFullPath() + " because " + ((Failure) obj).getMessage());
        }
    }

    @Override
    public void mvFile(final INode src, final INode dest) throws Exception {
        Object obj = sendReceive(new MoveMessage(new File(src), new File(dest)));
        if (obj instanceof Success) {
            System.out.println("Successfully moved from " + src.getFullPath() + " to " + dest.getFullPath());
        } else if (obj instanceof Failure) {
            throw new Exception("Failed to move from " + src.getFullPath() + " to " + dest.getFullPath() + " because " + ((Failure) obj).getMessage());
        }
    }

    @Override
    public Filesystem peek() {
        Object obj = sendReceive(new PeekMessageRequest());
        if (obj instanceof PeekMessageResponse) {
            PeekMessageResponse res = (PeekMessageResponse) obj;
            Filesystem f = new Filesystem(this);
            f.addSerializedNodes(res.getFiles());
            return f;
        } else if (obj instanceof Failure) {
            System.out.println("Failed to retrieve filesystem list from directory service: " + ((Failure) obj).getMessage());
        }
        return null;
    }

    public void addFile(final String path, final long size, java.io.File file) throws Exception {
        try {
            Socket conn = new Socket(InetAddress.getByName(host), port);
            Object obj = sendReceive(new AddFileMessage(path, size), conn);
            if (obj instanceof Failure) {
                System.out.println("Failed to communicate with directory service while moving file " + path + " because " + ((Failure) obj).getMessage());
            }
            FileInputStream s = new FileInputStream(file);
            BufferedInputStream bs = new BufferedInputStream(s);
            byte[] buf = new byte[1400];
            int bytesRead = 0;
            while ((bytesRead = bs.read(buf, 0, 1400)) != -1) {
                conn.getOutputStream().write(buf, 0, bytesRead);
                conn.getOutputStream().flush();
            }

        } catch (UnknownHostException e) {
            throw new Exception("Unable to find the selected directory service, maybe it has gone offline?");
        } catch (IOException e) {
            throw new Exception("Error communicating with selected directory service, maybe it has gone offline?");
        }
    }

    public void sendFile(final java.io.File file) throws Exception {
    }

    private Object sendReceive(final IMessage message) {
        try {
            Socket conn = new Socket(InetAddress.getByName(host), port);
            return sendReceive(message, conn);
        } catch (UnknownHostException e) {
            System.out.println("Unable to find the selected directory service, maybe it has gone offline?");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("There has been an unknown error.");
        }
        return null;
    }

    private Object sendReceive(final IMessage message, Socket conn) {
        try {
            ObjectOutputStream oStream = new ObjectOutputStream(conn.getOutputStream());
            oStream.writeObject(message);
            ObjectInputStream iStream = new ObjectInputStream(conn.getInputStream());
            try {
                Object obj = iStream.readObject();
                return obj;
            } catch (ClassNotFoundException e) {
                // No-op, this is indicative of worse problems
            }
        } catch (UnknownHostException e) {
            System.out.println("Unable to find the selected directory service, maybe it has gone offline?");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("There has been an unknown error.");
        }

        return null;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }
}
