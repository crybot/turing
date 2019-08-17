package turing.server.state;

import turing.chat.ChatMessage;
import turing.model.user.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Server-side in-memory state container.
 * ServerState is thread safe and shares its internal locks with all its instances.
 */
public class ServerState {
    //TODO: map users with users
    private Map<String, User> loggedUsers;
    private Map<String, Map<Integer, User>> editableSections;
    private Map<String, List<ChatMessage>> chatMessages;
    private Map<String, String> documentEditedByUser;

    // Global synchronization mechanisms
    private static ReadWriteLock lock;
    private static Lock readLock;
    private static Lock writeLock;

    public ServerState() {
        // Lock initialization
        if (lock == null) {
            lock = new ReentrantReadWriteLock();
            readLock = lock.readLock();
            writeLock = lock.writeLock();
        }

        loggedUsers = new ConcurrentHashMap<>();
        editableSections = new ConcurrentHashMap<>();
        chatMessages = new ConcurrentHashMap<>();
        documentEditedByUser = new ConcurrentHashMap<>();
    }

    /**
     * Get the User representation of a user among the logged ones
     * @param username
     * @return Optional.empty() if the user is not logged in
     */
    public Optional<User> getLoggedUser(String username) {
        readLock.lock();
        try {
            return Optional.ofNullable(loggedUsers.get(username));
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Check whether a given user is currently logged in
     * @param username
     * @return
     */
    public boolean isUserLoggedIn(String username) {
        readLock.lock();
        try {
            return getLoggedUser(username).isPresent();
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Log a user in
     * @param user
     */
    public void logUserIn(User user) {
        writeLock.lock();
        try {
            loggedUsers.put(user.name, user);
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Log a user out
     * @param user
     */
    public void logUserOut(User user) {
        writeLock.lock();
        try {
            loggedUsers.remove(user.name);
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Get the User object of the user editing a specific section of a document
     * @param documentName
     * @param section
     * @return Optional.empty() if the section is not being edited
     */
    public Optional<User> getUserEditingSection(String documentName, int section) {
        readLock.lock();
        try {
            if (editableSections.containsKey(documentName)) {
                return Optional.ofNullable(editableSections.get(documentName).getOrDefault(section, null));
            }
            return Optional.empty();
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Add a message to the chat of a given document
     * @param documentName
     * @param message
     */
    public void addChatMessage(String documentName, ChatMessage message) {
        writeLock.lock();
        try {
            chatMessages.putIfAbsent(documentName, new ArrayList<>());
            chatMessages.get(documentName).add(message);
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Retrieve all messages from the chat of a given document
     * @param documentName
     * @return
     */
    public List<ChatMessage> getChatMessages(String documentName) {
        readLock.lock();
        try {
            return chatMessages.getOrDefault(documentName, Collections.emptyList());
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Return the name of the document being edited by a given user
     * @param user
     * @return Optional.empty() if no such document is being edited by the user
     */
    public Optional<String> getDocumentNameEditedByUser(User user) {
        readLock.lock();
        try {
            return Optional.ofNullable(documentEditedByUser.getOrDefault(user.name, null));
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Check whether a section is being edited
     * @param documentName
     * @param section
     * @return
     */
    public boolean isSectionBeingEdited(String documentName, int section) {
        readLock.lock();
        try {
            if (!editableSections.containsKey(documentName)) {
                return false;
            }
            return editableSections.get(documentName).containsKey(section);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Set a section's state as being edited
     * @param documentName
     * @param section
     * @param userEditing
     */
    public void setEditingSection(String documentName, int section, User userEditing) {
        writeLock.lock();
        try {
            editableSections.putIfAbsent(documentName, new HashMap<>());
            if (editableSections.containsKey(documentName)) {
                editableSections.get(documentName).putIfAbsent(section, userEditing);
                documentEditedByUser.put(userEditing.name, documentName);
            }
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Set a section's state as not being edited
     * @param documentName
     * @param section
     * @param userEditing
     */
    public void unsetEditingSection(String documentName, int section, User userEditing) {
        writeLock.lock();
        try {
            if (editableSections.containsKey(documentName)) {
                editableSections.get(documentName).remove(section);
                documentEditedByUser.remove(userEditing.name); // must be present
            }
        }
        finally {
            writeLock.unlock();
        }
    }
}
