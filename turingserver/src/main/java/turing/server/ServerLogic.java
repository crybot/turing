package turing.server;

import jdk.jshell.spi.ExecutionControl;
import org.json.JSONObject;
import turing.communication.Communication;
import turing.communication.JsonPaylod;
import turing.communication.Message;
import turing.communication.tcp.TcpMessage;
import turing.model.JsonMapper;
import turing.model.document.Document;
import turing.model.invitation.Invitation;
import turing.model.user.User;
import turing.server.persistence.DocumentDataManager;
import turing.server.persistence.InvitationDataManager;
import turing.server.persistence.UserDataManager;
import turing.server.state.ServerState;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the decode-execute portion of the fetch-decode-execute cycle of networking requests processing
 */
public class ServerLogic {
    private Communication<JsonPaylod> communication;
    private UserDataManager userDataManager;
    private DocumentDataManager documentDataManager;
    private InvitationDataManager invitationDataManager;
    private ServerState serverState;

    public ServerLogic(ServerState serverState, Communication<JsonPaylod> communication) {
        this.serverState = serverState;
        this.communication = communication;
        userDataManager = new UserDataManager("./model/user/users");
        documentDataManager = new DocumentDataManager("./model/document/documents");
        invitationDataManager = new InvitationDataManager("./model/invitation/invitations");
    }

    /**
     * Decode a given message into a meaningful operation and execute it
     * @param message   the request message
     * @param communication the open communication on which to send a possible response
     */
    public void decodeAndExecute(Message<JsonPaylod> message,
                                 Communication<JsonPaylod> communication) {

        System.out.println("Decoding message: " + message.getContent().formatted());
        JSONObject json = message.getContent().getJson();

        try {
            // A login request contains the user's credentials
            if (json.has("login")) {
                var user = json.getJSONObject("login");
                login(user.getString("name"), user.getString("password"));
            }
            // A logout request contains a UUID parameter representing the ID of the user to be logged out
            else if (json.has("logout")) {
                // example json request: {"logout": {"userId": "xxxxx-yyyyy-wwwww-zzzzz"} }
                var userId = UUID.fromString(json.getJSONObject("logout").getString("userId"));
                var user = userDataManager.get(userId);
                // If a user with the given ID can be found in the database
                if (user.isPresent()) {
                    logout(user.get().name, user.get().password);
                }
                else {
                    throw new IOException("Could not find user with ID: " + userId);
                }
            }
            // A create request contains the document's name, an array of sections and the UUID of the file's author
            else if (json.has("create")) {
                // example json request: {"create": {"name": "foo",
                //                                  "sections": ["non", "empty", "section"],
                //                                  "authorId": "xxxxx-yyyyy-wwwww-zzzzz"}}
                var doc = json.getJSONObject("create");
                var name = doc.getString("name");
                var sections = doc.getJSONArray("sections");
                var authorId = UUID.fromString(doc.getString("authorId"));
                var author = userDataManager.get(authorId);
                if (author.isPresent()) {
                    createDocument(name, sections.length(), author.get());
                }
                else {
                    throw new IOException("Could not find user with ID: " + authorId);
                }
            }
            // A share request contains the document's name, the username to associate it with and the ID of the
            // document's author
            else if (json.has("share")) {
                // example json request: {"share": {"documentName": "foo", "userName": "bar"}}
                var invite = JsonMapper.fromJson(json.getJSONObject("share"), Invitation.class);
                var authorId = UUID.fromString(json.getJSONObject("share").getString("authorId"));
                var author = userDataManager.get(authorId);
                if (author.isPresent()) {
                    shareDocument(invite.documentName, invite.userName, author.get());
                }
                else {
                    throw new IOException("Could not find user with ID: " + authorId);
                }
            }
            // show section / show document
            // A show request contains either a document name and a section number, or just the document name.
            // In the first case only the section of the document matching the given section number have to be shown,
            // while in the latter, the entire document is shown.
            else if (json.has("show")) {
                var request = json.getJSONObject("show");
                var user = userDataManager.get(UUID.fromString(request.getString("userId")));

                if (request.has("documentName") && request.has("section") && user.isPresent()) {
                    showSection(request.getString("documentName"), request.getInt("section"), user.get());
                }
                else if (request.has("documentName") && user.isPresent()) {
                    showDocument(request.getString("documentName"), user.get());
                }
            }
            else {
                throw new IOException("error");
            }
        }
        catch (Exception e) {
            System.err.println("Could not process request: " + e.getLocalizedMessage());
            e.printStackTrace();
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
     * Response: If the login is successful, the server sends a response message containing the UUID of the user
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
                response = user.get().id.toString();
                ok = true;
            }
        }
        // Send the response to the client
        communication.sendMessage(TcpMessage.makeResponse(response, ok));
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
        // communication.sendMessage(new Message<>(JsonPaylod.makeResponse(response)));
    }

    /**
     * Create a document
     * @param name
     * @param sections
     * @param author
     */
    private void createDocument(String name, int sections, User author) throws IOException {
        // Default response
        String response = "User not logged in";
        boolean ok = false;

        // Check whether the user is logged in
        if (serverState.isUserLoggedIn(author.name)) {
            // Try to create a new document
            var documentId = documentDataManager.create(new Document(name, sections, author.id));
            // If the document has been created correctly
            if (documentId.isPresent()) {
                response = "Document created successfully with name: " + name;
                ok = true;
            }
            else {
                response = "Could not create document";
                ok = false;
            }
        }
        // Send response
        communication.sendMessage(TcpMessage.makeResponse(response, ok));
    }

    //TODO: make sure only the real owner of the file can share a document!
    private void shareDocument(String documentName, String userName, User author) throws IOException {
        // Default response
        String response = "User not logged in";
        boolean ok = false;

        if (serverState.isUserLoggedIn(author.name)) {
            var invitationId = invitationDataManager.create(new Invitation(documentName, userName));
            if (invitationId.isPresent()) {
                response = "Document " + documentName + " successfully shared with " + userName;
                ok = true;
            }
            else {
                response = "Could not share document";
                ok = false;
            }
        }
        // Send response
        communication.sendMessage(TcpMessage.makeResponse(response, ok));
    }

    /**
     * Send the client the content of the indicated section of the selected document
     * Note: The user must have permissions to read the document.
     * @param documentName  the name of the document
     * @param section       the document's section's number
     * @param user          the user sending the request
     */
    private void showSection(String documentName, int section, User user) throws IOException {
        // Default response
        String response = "User not logged in";
        boolean ok = false;

        if (serverState.isUserLoggedIn(user.name)) {
            var userDocuments = getUserDocuments(user);
            // If the user has the rights to read the document
            if (userDocuments.stream().anyMatch(doc -> doc.getName().equals(documentName))) {
                var document = documentDataManager.getByName(documentName);
                // Check whether the document (redundant) and the selected section exists
                if (document.isPresent() && document.get().getSection(section).isPresent()) {
                    response = document.get().getSection(section).get();
                    ok = true;
                }
                else {
                    response = "Could not open section " + section + " of document " + documentName;
                    ok = false;
                }
            }
            else {
                response = "User has no rights to access the document";
                ok = false;
            }
        }
        // Send response
        communication.sendMessage(TcpMessage.makeResponse(response, ok));
    }

    private void showDocument(String documentName, User user) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("showDocument() not implemented");
    }

    /**
     * Retrieve documents accessible by the user either because he is the owner of the document or because
     * he received an invitation by an other user
     * @param user
     * @return A list of all documents accessible by the user
     */
    private List<Document> getUserDocuments(User user) {
        // Get documents directly created by the user
        var ownedDocuments = documentDataManager.getByAuthor(user);
        // Get documents for which the user received an invitation:
        //  - first get all invitations received by the user;
        //  - get all (optional) documents indicated by the invitations;
        //  - flatten the list of Optional<Document>-s to a list Document-s
        //      (basically an application of the binding operator to a monad)
        var sharedDocuments = invitationDataManager.getByUser(user)
                .stream()
                .map(inv -> documentDataManager.getByName(inv.documentName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // concat the two lists and return the result
        ownedDocuments.addAll(sharedDocuments);
        return ownedDocuments;
    }
}
