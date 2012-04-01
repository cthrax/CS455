package cdn.shared.message.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cdn.shared.message.IMessage;

public class RegisterResponseMessage implements IMessage {

    public StatusCode status;
    public String additionalInfo;

    public RegisterResponseMessage(StatusCode status, String additionalInfo) {
        this.status = status;
        this.additionalInfo = additionalInfo;
    }

    public RegisterResponseMessage(byte[] bytes) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        try {
            DataInputStream s = new DataInputStream(stream);
            status = StatusCode.fromOrdinal(s.readInt());
            additionalInfo = s.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public MessageType getType() {
        return MessageType.REGISTER_RESPONSE;
    }

    @Override
    public byte[] getWireFormat() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DataOutputStream s = new DataOutputStream(stream);
            s.writeInt(getType().ordinal());
            s.writeInt(status.ordinal());
            s.writeUTF(additionalInfo);
            s.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return stream.toByteArray();
    }

}
