package turing.server;

import org.json.JSONObject;
import turing.communication.Communication;
import turing.communication.Message;
import turing.communication.Payload;
import turing.communication.TuringPayload;
import turing.communication.tcp.TcpMessage;
import turing.model.user.User;
import turing.server.persistence.DataManager;
import turing.server.persistence.UserDataManager;
import turing.server.state.ServerState;

import java.io.IOException;
import java.util.Optional;

/**
 * Handles the decode-execute portion of the fetch-decode-execute cycle of networking requests processing
 */
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
     * Decode a given message into a meaningful operation and execute it
     * @param message   the request message
     * @param communication the open communication on which to send a possible response
     */
    public void decodeAndExecute(Message<TuringPayload> message,
                                 Communication<TuringPayload> communication) {

        System.out.println("Decoding message: " + message.getContent().formatted());
        JSONObject json = message.getContent().getJson();

        try {
            if (json.has("login")) {
                var user = json.getJSONObject("login");
                login(user.getString("name"), user.getString("password"));
            } else if (json.has("logout")) {
                var user = json.getJSONObject("logout");
                logout(user.getString("name"), user.getString("password"));
            }
        }
        catch (IOException e) {
            System.err.println("Could not process request: " + e.getLocalizedMessage());
            communication.trySendMessage(TcpMessage.makeResponse("Internal server error", false));
        }
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
    private void login(String username, String password) throws IOException {
        System.out.println("User " + username  + " requested to login" );

        // Default response
        String response = "Could not log in";
        boolean ok = false;

        // Check whether the user is already logged in
        if (serverState.isUserLoggedIn(username)) {
            response = "Already logged in";
            ok = false; // redundant, but easy on the eyes
        }
        else {
            // Get an user matched by the given username and password
            Optional<User> user = userDataManager.getByNameAndPassword(username, password);
            // If such user is present
            if (user.isPresent()) {
                serverState.logUserIn(user.get()); // Log the user in
                response = "Login successful";
                ok = true;
            }
        }
        // Send the response to the client
        communication.sendMessage(TcpMessage.makeResponse(response, ok));
        // communication.sendMessage(new Message<>(TuringPayload.makeResponse(response)));
    }

    private void logout(String username, String password) throws IOException {
        System.out.println("User " + username  + " requested to logout" );

        // Default response
        String response = "Could not logout";
        boolean ok = false;

        // Check whether the user is logged in
        if (serverState.isUserLoggedIn(username)) {
            // Get an user matched by the given username and password
            Optional<User> user = userDataManager.getByNameAndPassword(username, password);
            // If such user is present
            if (user.isPresent()) {
                serverState.logUserOut(user.get());
                response = "Logout successful";
                ok = true;
            }
        }
        else {
            response = "User not logged in";
            ok = false;
        }
        communication.sendMessage(TcpMessage.makeResponse(response, ok));
        // communication.sendMessage(new Message<>(TuringPayload.makeResponse(response)));
    }
}
