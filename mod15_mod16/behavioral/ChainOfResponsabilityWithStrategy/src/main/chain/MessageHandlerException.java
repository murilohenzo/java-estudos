package main.chain;

public class MessageHandlerException extends RuntimeException {
    public MessageHandlerException(String message) {
        super(message);
    }

    public MessageHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
