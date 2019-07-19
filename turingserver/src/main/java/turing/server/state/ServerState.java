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

    public ServerState() {
        loggedUsers = new HashMap<>();
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


}
