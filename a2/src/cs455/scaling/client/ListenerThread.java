package cs455.scaling.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import cs455.scaling.shared.GlobalLogger;

public class ListenerThread extends Thread {
    private final BufferedInputStream reader;
    List<String> hashes = new LinkedList<String>();

    public ListenerThread(BufferedInputStream reader) {
        super("Listener Thread");
        this.reader = reader;
    }

    public void addHash(byte[] hash) {
        String hex = new BigInteger(1, hash).toString(16);
        synchronized (hashes) {
            hashes.add(hex);
        }
    }

    @Override
    public void run() {
        byte[] retHash = new byte[20];
        try {
            while (!interrupted()) {
                int read = reader.read(retHash, 0, 20);
                if (read > 0) {
                    String hex = new BigInteger(1, retHash).toString(16);
                    synchronized (hashes) {
                        int idx = hashes.indexOf(hex);
                        if (idx >= 0) {
                            GlobalLogger.info(this, "Successfully matched " + hex);
                            hashes.remove(idx);
                        } else {
                            GlobalLogger.debug(this, "Invalid hash received " + hex);
                        }
                    }
                } else {
                    GlobalLogger.warning(this, "Read " + read + " bytes");
                    if (read < 0) {
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            GlobalLogger.severe(this, "Failed to read response from server.");
        }
    }
}
