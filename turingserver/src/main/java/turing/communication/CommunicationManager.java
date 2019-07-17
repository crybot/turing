package turing.communication;

import turing.network.Entity;

import java.util.Collection;
import java.util.HashSet;

/**
 * Handles communication between network processes
 */
public abstract class CommunicationManager {
    private Collection<Entity> recipients;

    protected CommunicationManager() {
        recipients = new HashSet<>();
    }

    /**
     * Adds a recipient to the destination list
     * @param recipient
     */
    public void addRecipient(Entity recipient) {
        recipients.add(recipient);
    }

    // public abstract Message receiveMessage();

    // public abstract void sendMessage(Message message);
}
