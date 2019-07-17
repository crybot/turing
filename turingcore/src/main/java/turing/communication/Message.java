package turing.communication;

/**
 * Object representation of a network message
 * @param <T>   the type of the message's body
 */
public class Message<T extends Payload> {
    private T content;

    public Message(T content) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }
}
