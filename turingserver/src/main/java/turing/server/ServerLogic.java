package turing.server;

import org.json.JSONObject;
import turing.chat.ChatMessage;
import turing.communication.Communication;
import turing.communication.JsonPayload;
import turing.communication.Message;
import turing.communication.tcp.TcpMessage;
import turing.communication.udp.UdpMessage;
import turing.model.JsonMapper;
import turing.model.document.Document;
import turing.model.invitation.Invitation;
import turing.model.user.User;
import turing.server.persistence.DocumentDataManager;
import turing.server.persistence.InvitationDataManager;
import turing.server.persistence.UserDataManager;
import turing.server.state.ServerState;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the decode-execute portion of the fetch-decode-execute cycle of networking requests processing
 */
public class ServerLogic {
    private Communication<JsonPayload> communication;
    private UserDataManager userDataManager;
    private DocumentDataManager documentDataManager;
    private InvitationDataManager invitationDataManager;
    private ServerState serverState;

    public ServerLogic(ServerState serverState, Communication<JsonPayload> communication) {
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
    public void decodeAndExecute(Message<JsonPayload> message,
                                 Communication<JsonPayload> communication) {

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
                var authorId = UUID.fromString(json.getJSONObject("share").getString("userId"));
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
                else {
                    throw new IOException();
                }
            }
            else if (json.has("list")) {
                var request = json.getJSONObject("list");
                var user = userDataManager.get(UUID.fromString(request.getString("userId")));
                if (user.isPresent()) {
                    listDocuments(user.get());
                }
                else {
                    throw new IOException("Could not find user");
                }
            }
            else if (json.has("edit")) {
                var request = json.getJSONObject("edit");
                var user = userDataManager.get(UUID.fromString(request.getString("userId")));
                if (user.isPresent()) {
                    editSection(request.getString("documentName"), request.getInt("section"), user.get());
                }
                else {
                    throw new IOException("Could not find user");
                }
            }
            else if (json.has("endEdit")) {
                var request = json.getJSONObject("endEdit");
                var content = request.getString("content");
                var user = userDataManager.get(UUID.fromString(request.getString("userId")));
                if (user.isPresent()) {
                    endEditSection(request.getString("documentName"), request.getInt("section"), content, user.get());
                }
                else {
                    throw new IOException("Could not find user");
                }
            }
            else if (json.has("send")) {
                var request = json.getJSONObject("send");
                var content = request.getString("message");
                var user = userDataManager.get(UUID.fromString(request.getString("userId")));
                if (user.isPresent()) {
                    sendChatMessage(content, user.get());
                }
            }
            else if (json.has("receive")) {
                var userId = json.getJSONObject("receive").getString("userId");
                var user = userDataManager.get(UUID.fromString(userId));
                if (user.isPresent()) {
                    receiveChatMessages(user.get());
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

    /**
     * Log out the given user
     * The user have to be registered to the service.
     * The user must be already logged in.
     * @param username
     * @param password
     * @throws IOException
     */
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
        // communication.sendMessage(new Message<>(JsonPayload.makeResponse(response)));
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
    private boolean showSection(String documentName, int section, User user) throws IOException {
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
        return ok;
    }

    /**
     * Send the client the content of the selected document
     * Note: The user must have permissions to read the document.
     * @param documentName  the name of the document
     * @param user          the user sending the request
     * @throws IOException
     */
    private void showDocument(String documentName, User user) throws IOException {
        // Default response
        String response = "User not logged in";
        boolean ok = false;

        if (serverState.isUserLoggedIn(user.name)) {
            var userDocuments = getUserDocuments(user);
            // If the user has the rights to read the document
            if (userDocuments.stream().anyMatch(doc -> doc.getName().equals(documentName))) {
                var document = documentDataManager.getByName(documentName);
                // Check whether the document (redundant) and the selected section exists
                if (document.isPresent()) {
                    response = document.get().toString();
                    ok = true;
                }
                else {
                    response = "Could not open document " + documentName;
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

    /**
     * Send the client the list of documents accessible by the user
     * @param user          the user sending the request
     * @throws IOException
     */
    private void listDocuments(User user) throws IOException {
        // Default response
        String response = "User not logged in";
        boolean ok = false;

        if (serverState.isUserLoggedIn(user.name)) {
            var userDocuments = getUserDocuments(user).stream().map(Document::getName).collect(Collectors.toList());
            response = String.join(System.lineSeparator(), userDocuments);
            ok = true;
        }

        // Send response
        communication.sendMessage(TcpMessage.makeResponse(response, ok));
    }

    /**
     * Send the client the current working copy of the section of choice and allow the user to modify it.
     * To modify the section, an endEditSection request must be sent to the server by the client, with the modified text
     * as parameter
     * @param documentName  the name of the document
     * @param section       the document's section's number
     * @param user          the user sending the request
     */
    //TODO: make sure the user is only editing one section at a time
    private void editSection(String documentName, int section, User user) throws IOException {
        if (serverState.isSectionBeingEdited(documentName, section)) {
            communication.sendMessage(TcpMessage.makeResponse("Section already being edited", false));
        }
        // showSection handles user conditions (is he logged in, does he have permissions...)
        else if (showSection(documentName, section, user)) {
            serverState.setEditingSection(documentName, section, user);
        }
    }

    /**
     * Make the changes to the selected section permanent.
     * An edit section request must have been previously performed by the same user making the end edit request.
     * @param documentName  the name of the document
     * @param section       the document's section's number
     * @param content       the new content of the edited section
     * @param user          the user sending the request
     * @throws IOException
     */
    private void endEditSection(String documentName, int section, String content, User user) throws IOException {
        String response;
        boolean ok;

        // If there exists a document with the given name and
        // the selected section is being edited by the same user making the request
        if (documentDataManager.getByName(documentName).isPresent() &&
                user.equals(serverState.getUserEditingSection(documentName, section).orElse(null))) {
            // Mark the section as not being edited anymore
            serverState.unsetEditingSection(documentName, section, user);
            // Get the document object as we know it exists
            Document doc =  documentDataManager.getByName(documentName).get();
            doc.setSection(section, content);
            documentDataManager.update(doc);
            response = "Section " + section +
                    " of document " + documentName +
                    " correctly updated";
            ok = true;
        }
        else {
            response = "User not editing the document";
            ok = false;
        }

        communication.sendMessage(TcpMessage.makeResponse(response, ok));
    }

    /**
     * Send a message to the group chat of the document edited by the user making the request.
     * If the user is not editing any document, the request is rejected and no message is sent to the chat.
     * @param message
     * @param user
     */
    private void sendChatMessage(String message, User user) throws IOException {
        ChatMessage chatMessage = new ChatMessage(user, message);
        Optional<String> documentName = serverState.getDocumentNameEditedByUser(user);
        String response = "Could not forward the chat message";
        boolean ok = false;

        // If the user is editing a document
        if (documentName.isPresent()) {
            // Store the chat message in memory
            serverState.addChatMessage(documentName.get(), chatMessage);
            response = "Message successfully sent to the document's chat";
            ok = true;
        }

        communication.sendMessage(UdpMessage.makeResponse(response, ok));
    }

    /**
     *
     * @param user
     * @throws IOException
     */
    private void receiveChatMessages(User user) throws  IOException {
        Optional<String> documentName = serverState.getDocumentNameEditedByUser(user);
        String response;
        boolean ok = false;

        // If the user is editing a document
        if (documentName.isPresent()) {
            // Store the chat message in memory
            Collection<ChatMessage> messages = serverState.getChatMessages(documentName.get());
            response = messages.stream().map(ChatMessage::toString).collect(Collectors.joining(System.lineSeparator()));
            ok = true;
        }
        else {
            response = "User not editing any document";
        }

        communication.sendMessage(UdpMessage.makeResponse(response, ok));
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
        //  - filter out documents the user already owns
        var sharedDocuments = invitationDataManager.getByUser(user)
                .stream()
                .map(inv -> documentDataManager.getByName(inv.documentName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(doc -> !ownedDocuments.contains(doc))
                .collect(Collectors.toList());

        // concat the two lists and return the result
        ownedDocuments.addAll(sharedDocuments);
        return ownedDocuments;
    }
}
