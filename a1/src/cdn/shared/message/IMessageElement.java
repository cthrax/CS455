package cdn.shared.message;


public interface IMessageElement {
    /**
     * Gets the format for sending across the wire.
     * 
     * @return the format for sending across the wire.
     */
    public byte[] getWireFormat();
}
