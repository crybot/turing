package turing.communication;

import java.io.Serializable;

/**
 * Abstraction representing the content of a message.
 * Must be serializable in order to be shipped through the network.
 */
public interface Payload extends Serializable {
    //TODO: reason about functionalities

    /**
     * @return a formatted version of the payload
     */
    String formatted();

    /**
     * @return the payload content as a byte array
     */
    default byte[] bytes() {
        return formatted().getBytes();
    }
}
