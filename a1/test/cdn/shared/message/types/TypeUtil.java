package cdn.shared.message.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.DataOutputStream;

public class TypeUtil {
    /**
     * Create a new linkInfo object with the passed in info and two routers with the following info:
     * Router # | id | hostname | port |
     * 1 | id1 | 1.1.1.1 | 8000 |
     * 2 | id2 | 1.1.1.2 | 8001 |
     *
     * @param weight the weight to give the link.
     * @param id the id base for the routers.
     * @return the new LinkInfo Object.
     */
    public static LinkInfo createLinkInfo(int weight, String id) {
        RouterInfo[] routers = new RouterInfo[2];
        for (int i = 0; i < 2; i++) {
            routers[i] = TypeUtil.createRouterInfo(id + i, "1.1.1." + i, 8000 + i);
        }

        return new LinkInfo(weight, routers);
    }

    /**
     * Create a new routerInfo object with the given info.
     * 
     * @param id the id of the router.
     * @param hostname the hostname of the router.
     * @param port the port for the router.
     * @return the new routerInfo.
     */
    public static RouterInfo createRouterInfo(String id, String hostname, int port) {
        return new RouterInfo(id, hostname, port);
    }

    /**
     * Get a byte[] representing a serialized LinkInfo object.
     * 
     * @param weight the weight of the linkInfo to serialize
     * @param id the id base to use for the created RouterInfo objects.
     * @return the new byte[] with the given info.
     */
    public static byte[] getLinkInfoBits(int weight, String id) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);
            s.writeInt(weight);

            // Create some routers.
            for (int i = 0; i < 2; i++) {
                byte[] bytes = TypeUtil.getRouterBits(id + i, "1.1.1." + i, 8000 + i);
                s.writeInt(bytes.length);
                s.write(bytes);
            }
            s.flush();
        } catch (IOException e) {
            System.out.println("ERROR 2:");
            e.printStackTrace();
        }
        return stream.toByteArray();
    }

    /**
     * Get a byte[] representing the serialized form of a RouterInfo object.
     * 
     * @param id the id of the router.
     * @param hostname the hostname of hte router.
     * @param port the port number that the router is at.
     * @return the new byte[] representing the RouterInfo object.
     */
    public static byte[] getRouterBits(String id, String hostname, int port) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);
            s.writeUTF(id);
            s.writeUTF(hostname);
            s.writeInt(port);
            s.flush();
        } catch (IOException e) {
            System.out.println("ERROR 3:");
            e.printStackTrace();
        }

        return stream.toByteArray();
    }
}
