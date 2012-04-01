package cdn.shared.message.types;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.DataOutputStream;

import org.testng.annotations.Test;

import cdn.shared.message.IMessage.MessageType;

public class TestLinkWeightsUpdateMessage {

    @Test
    public void testDeserialize() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);
            int numberOfLinks = 3;
            s.writeInt(3);

            String[] ids = new String[] { "a", "b", "c" };
            for (int i = 0; i < numberOfLinks; i++) {
                int weight = (int)(Math.random() * 100);
                byte[] bytes = TypeUtil.getLinkInfoBits(weight, ids[i % ids.length]);
                s.writeInt(bytes.length);
                s.write(bytes);
            }
            s.flush();

            LinkWeightsUpdateMessage message = new LinkWeightsUpdateMessage(stream.toByteArray());
            assertEquals(message.getLinkCount(), numberOfLinks);
            assertEquals(message.getLinks().length, numberOfLinks);
        } catch (IOException e) {
            System.out.println("ERROR 1:");
            e.printStackTrace();
        }
    }

    @Test
    public void testSerialize() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream s;
        try {
            s = new DataOutputStream(stream);
            int numberOfLinks = 3;
            s.writeInt(MessageType.LINK_WEIGHT_UPDATE.ordinal());
            s.writeInt(numberOfLinks);
            LinkInfo[] links = new LinkInfo[numberOfLinks];

            String[] ids = new String[] { "a", "b", "c" };
            for (int i = 0; i < numberOfLinks; i++) {
                int weight = (int) (Math.random() * 100);
                String id = ids[i % ids.length];
                byte[] bytes = TypeUtil.getLinkInfoBits(weight, id);
                s.writeInt(bytes.length);
                s.write(bytes);

                links[i] = TypeUtil.createLinkInfo(weight, id);
            }

            s.flush();
            LinkWeightsUpdateMessage message = new LinkWeightsUpdateMessage(links);
            byte[] expected = stream.toByteArray();
            byte[] actual = message.getWireFormat();
            assertEquals(actual, expected);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
