package turing.server.state;

import turing.model.user.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-side in-memory state container.
 */
// TODO: implement read-write locking strategy
public class ServerState {
    //TODO: map users with users
    private Map<String, User> loggedUsers;
    private Map<String, Map<Integer, User>> editableSections;

    public ServerState() {
        loggedUsers = new HashMap<>();
        editableSections = new HashMap<>();
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

    public boolean isSectionBeingEdited(String documentName, int section) {
        if (!editableSections.containsKey(documentName)) {
            return false;
        }
        return editableSections.get(documentName).containsKey(section);
    }

    public void setSectionEditable(String documentName, int section, User userEditing) {
        editableSections.putIfAbsent(documentName, new HashMap<>());
        if (editableSections.containsKey(documentName)) {
            editableSections.get(documentName).putIfAbsent(section, userEditing);
        }
    }

    public void setSectionNotEditable(String documentName, int section) {
        if (editableSections.containsKey(documentName)) {
            editableSections.get(documentName).remove(section);
        }
    }
}
