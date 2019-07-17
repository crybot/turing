package turing.server;

import turing.communication.Communication;
import turing.communication.Message;
import turing.communication.Payload;
import turing.communication.TuringPayload;
import turing.model.user.User;
import turing.server.persistence.DataManager;
import turing.server.persistence.UserDataManager;
import turing.server.state.ServerState;

import java.io.IOException;
import java.util.Optional;

public class ServerLogic {
    private Communication<TuringPayload> communication;
    private UserDataManager userDataManager;
    private ServerState serverState;

    public ServerLogic(ServerState serverState, Communication<TuringPayload> communication) {
        this.serverState = serverState;
        this.communication = communication;
        userDataManager = new UserDataManager();
    }

    /**
     * Log-in the given user.
     * The user have to be registered to the service.
     * The user must not be already logged in.
     * Security notes: There is an obvious security pitfall in the way user logging is handled;
     *                 After a user logs in, the same account can then be used from other clients without the need
     *                 to authenticate again. This problem would be solved by using an authentication method based
     *                 on security tokens like OAuth2.0 or by caching cookies on the client machine.
     *                 Both methods have been considered overkill for the overall project, which is of didactic nature.
     * @param username
     * @param password
     * @throws IOException
     */
    public void login(String username, String password) throws IOException {
        System.out.println("User " + username  + " requested login with password " + password);

        // Default response
        String response = "Could not log in";

        // Check whether the user is already logged in
        if (serverState.isUserLoggedIn(username)) {
            response = "Already logged in.";
        }
        else {
            // Get an user matched by the given username and password
            Optional<User> user = userDataManager.getByNameAndPassword(username, password);
            // If such user is present
            if (user.isPresent()) {
                serverState.logUserIn(user.get()); // Log the user in
                response = "Login successful";
            }
        }
        // Send the response to the client
        communication.sendMessage(new Message<>(TuringPayload.makeResponse(response)));
    }
}
