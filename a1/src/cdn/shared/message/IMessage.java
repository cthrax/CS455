package cdn.shared.message;


public interface IMessage extends IMessageElement {
    public enum MessageType {
        REGISTER_REQUEST,
        REGISTER_RESPONSE,
        DEREGISTER_REQUEST,
        DEREGISTER_RESPONSE,
        PEER_ROUTER_LIST,
        LINK_WEIGHT_UPDATE,
        ROUTER_CONNECTION,
        PASSING_DATA,
        INVALID;

        public static MessageType fromOrdinal(int ordinal) {
            if (ordinal >= 0 && ordinal < MessageType.values().length) {
                return MessageType.values()[ordinal];
            } else {
                return MessageType.INVALID;
            }
        }
    };

    public enum StatusCode {
        SUCCESS,
        FAILURE,
        INVALID;

        public static StatusCode fromOrdinal(int ordinal) {
            if (ordinal >= 0 && ordinal < StatusCode.values().length) {
                return StatusCode.values()[ordinal];
            } else {
                return StatusCode.INVALID;
            }
        }
    }

    MessageType getType();
}
