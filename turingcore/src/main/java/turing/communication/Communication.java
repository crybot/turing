package turing.communication;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

public interface Communication<T extends Payload> extends Closeable {

    /**
     * Retrieves a message from the source of the communication
     * No conjecture is being made on the synchrony or asynchrony of the operation.
     * @return a message if one is available.
     */
    Optional<Message<T>> consumeMessage() throws IOException;

    /**
     * Send a message to the configured recipients.
     * No conjecture is being made on the synchrony or asynchrony of the operation.
     */
    void sendMessage(Message<T> message) throws IOException;
}
