package turing.server.state;

import turing.chat.ChatMessage;
import turing.model.user.User;

import java.util.*;

/**
 * Server-side in-memory state container.
 */
// TODO: implement read-write locking strategy
public class ServerState {
    //TODO: map users with users
    private Map<String, User> loggedUsers;
    private Map<String, Map<Integer, User>> editableSections;
    private Map<String, List<ChatMessage>> chatMessages;
    private Map<String, String> documentEditedByUser;

    public ServerState() {
        loggedUsers = new HashMap<>();
        editableSections = new HashMap<>();
        chatMessages = new HashMap<>();
        documentEditedByUser = new HashMap<>();
    }

    public Optional<User> getLoggedUser(String username) {
        return Optional.ofNullable(loggedUsers.get(username));
    }

    public boolean isUserLoggedIn(String username) {
        return getLoggedUser(username).isPresent();
    }

    public void logUserIn(User user) {
        loggedUsers.put(user.name, user);
    }

    public void logUserOut(User user) {
        loggedUsers.remove(user.name);
    }

    public Optional<User> getUserEditingSection(String documentName, int section) {
        if (editableSections.containsKey(documentName)) {
            return Optional.ofNullable(editableSections.get(documentName).getOrDefault(section, null));
        }
        return Optional.empty();
    }

    public Collection<User> getUsersEditingDocument(String documentName) {
        if (editableSections.containsKey(documentName)) {
            return editableSections.get(documentName).values();
        }
        return Collections.emptyList();
    }

    public void addChatMessage(String documentName, ChatMessage message) {
        chatMessages.putIfAbsent(documentName, new ArrayList<>());
        chatMessages.get(documentName).add(message);
    }

    public List<ChatMessage> getChatMessages(String documentName) {
        return chatMessages.getOrDefault(documentName, Collections.emptyList());
    }

    public Optional<String> getDocumentNameEditedByUser(User user) {
        return Optional.ofNullable(documentEditedByUser.getOrDefault(user.name, null));
    }

    public boolean isSectionBeingEdited(String documentName, int section) {
        if (!editableSections.containsKey(documentName)) {
            return false;
        }
        return editableSections.get(documentName).containsKey(section);
    }

    public void setEditingSection(String documentName, int section, User userEditing) {
        editableSections.putIfAbsent(documentName, new HashMap<>());
        if (editableSections.containsKey(documentName)) {
            editableSections.get(documentName).putIfAbsent(section, userEditing);
            documentEditedByUser.put(userEditing.name, documentName);
        }
    }

    public void unsetEditingSection(String documentName, int section, User userEditing) {
        if (editableSections.containsKey(documentName)) {
            editableSections.get(documentName).remove(section);
            documentEditedByUser.remove(userEditing.name); // must be present
        }
    }
}
