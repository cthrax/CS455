package cs455.scaling.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import cs455.scaling.shared.GlobalLogger;

public class Client {

    private final InetAddress serverHost;
    private final int serverPort;
    private final int messageRate;

    public Client(InetAddress serverHost, int serverPort, int messageRate) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.messageRate = messageRate;
    }

    public void run() {
        Socket server = new Socket();
        try {
            server.setKeepAlive(true);
        } catch (SocketException e) {
            GlobalLogger.warning(this, "Failed to set keep-alive to true on socket.");
        }

        try {
            server.connect(new InetSocketAddress(serverHost, serverPort));
        } catch (IOException e) {
            GlobalLogger.severe(this, "Failed to connect to server, exiting...");
            return;
        }

        byte[] data = new byte[8192];
        byte[] hash = new byte[20];
        Random random = new Random(new Date().getTime());
        BufferedOutputStream out;
        try {
            out = new BufferedOutputStream(server.getOutputStream());
        } catch (IOException e) {
            GlobalLogger.severe(this, "Failed to get server output stream, exiting...");
            return;
        }

        BufferedInputStream in;
        try {
            in = new BufferedInputStream(server.getInputStream());
        } catch (IOException e) {
            GlobalLogger.severe(this, "Failed to get server input stream, exiting...");
            return;
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e1) {
            GlobalLogger.severe(this, "Failed to get digest algorithm, exiting...");
            return;
        }

        ListenerThread listener = new ListenerThread(in);
        listener.start();

        try {
            String host = InetAddress.getLocalHost().getHostName();
            GlobalLogger.info(this, "Client running at " + host + " with a refresh rate of " + messageRate);
        } catch (UnknownHostException ex) {
            GlobalLogger.info(this, "Client running with message rate of " + messageRate);
        }

        while (true) {
            random.nextBytes(data);
            // Calculate digest
            hash = digest.digest(data);
            // Add to the response list
            listener.addHash(hash);

            try {
                GlobalLogger.info(this, "Sending " + new BigInteger(1, hash).toString(16));
                out.write(data, 0, data.length);
                out.flush();
            } catch (IOException e) {
                GlobalLogger.severe(this, "Failed to write bytes to server.");
            }

            try {
                Thread.sleep(1000 / messageRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ClientArgumentParser parser = new ClientArgumentParser(args);
        if (parser.isValid()) {
            Client client = new Client(parser.getServerHost(), parser.getServerPort(), parser.getMessageRate());
            client.run();
        }
    }
}
