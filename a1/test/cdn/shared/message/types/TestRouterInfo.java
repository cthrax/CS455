package cdn.shared.message.types;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.DataOutputStream;

import org.testng.annotations.Test;

public class TestRouterInfo {
    @Test
    public void testDeserialization() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);
            String id = "id";
            String hostname = "1.1.1.0";
            int port = 8000;
            s.writeUTF(id);
            s.writeUTF(hostname);
            s.writeInt(port);
            s.flush();
            RouterInfo router = new RouterInfo(stream.toByteArray());
            assertEquals(router.getId(), id);
            assertEquals(router.getHostname(), hostname);
            assertEquals(router.getPort(), port);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testSerialization() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);
            String id = "id";
            String hostname = "1.1.1.100";
            int port = 8000;
            s.writeUTF(id);
            s.writeUTF(hostname);
            s.writeInt(port);
            s.flush();

            RouterInfo router = new RouterInfo(id, hostname, port);
            assertEquals(router.getWireFormat(), stream.toByteArray());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
