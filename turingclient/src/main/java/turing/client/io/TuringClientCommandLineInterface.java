package turing.client.io;

import jdk.jshell.spi.ExecutionControl;
import org.json.JSONObject;
import turing.communication.tcp.TcpCommunication;
import turing.communication.tcp.TcpMessage;
import turing.model.document.Document;
import turing.model.invitation.Invitation;
import turing.model.user.User;
import turing.util.stream.StreamUtils;

import java.io.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Exposes and implements Client services for the Turing application
 */
public class TuringClientCommandLineInterface implements ClientUserInterface {
    private static final File cachedUserId = new File("./cache/userId");

    public TuringClientCommandLineInterface() { }

    /**
     * Store users's UUID in a persistent location for later work.
     * The stored ID is transparently used by the logout procedure.
     * Only one user at a time can store his ID in the cached file.
     * @param userId    the UUID to be stored
     * @throws IOException  if the credentials file does not exist and cannot be created
     */
    private void saveUserId(UUID userId) throws IOException {
        // Create userId file (and its parent directories) if it does not exist
        if (!cachedUserId.exists()) {
            cachedUserId.getParentFile().mkdirs();
            Files.createFile(cachedUserId.toPath());
        }
        ByteBuffer buffer = ByteBuffer.wrap(userId.toString().getBytes());
        FileChannel channel = new FileOutputStream(cachedUserId).getChannel();
        channel.write(buffer);
        channel.close();
    }

    /**
     * Retrieve the previously stored user's UUID
     * @return  user's ID wrapped in an Optional.
     */
    private Optional<UUID> getSavedUserId() {
        if (!cachedUserId.exists()) {
            return Optional.empty();
        }
        try(FileChannel channel = FileChannel.open(cachedUserId.toPath())) {
            var buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, cachedUserId.length());
            // Decode the bytebuffer with the default charset
            Charset charset = Charset.defaultCharset();
            CharsetDecoder decoder = charset.newDecoder();

            UUID userId = UUID.fromString(decoder.decode(buffer).toString());
            return Optional.of(userId);
        }
        catch (Exception e) {
            return Optional.empty();
        }

    }


    @Override
    public void register(String username, String password) {

    }

    //TODO: try erasing cached User ID and then make a request
    private Optional<String> sendRequest(String requestName, JSONObject parameters, String successString,
                                         String failureString, boolean includeAuth) {
        try {
            // Attach UserId (Auth) to the request body
            if (includeAuth) {
                Optional<UUID> userId = getSavedUserId();
                if (!userId.isPresent()) {
                    throw new IllegalStateException("Devi prima effettuare il login.");
                }
                else {
                    parameters.put("userId", userId.get().toString());
                }
            }

            // make payload
            JSONObject payload = new JSONObject().put(requestName, parameters);

            // Establish communication channel with the server
            var communication = new TcpCommunication(InetAddress.getLocalHost(), 1024);
            // Send request message to the server
            communication.sendMessage(TcpMessage.makeRequest(payload));
            // Receive response from the server
            Optional<TcpMessage> response = communication.consumeMessage();

            // If any response has been received
            if (response.isPresent()) {
                // If the response contains any content
                if (response.get().getOk()) {
                    System.out.println(successString);
                    return response.get().getResponse();
                }
                else {
                    System.err.println(failureString + ": "
                            + response.get().getResponse().orElse("Errore non specificato"));
                    return Optional.empty();
                }
            }
        }
        catch (Exception e) {
            System.err.println(failureString + ": " + e.getLocalizedMessage());
        }

        return Optional.empty();
    }

    /**
     * Send a login request to the server and wait for a response.
     * If any response is received, it will be printed on screen.
     * Note: In case of a successful login, the server returns an unique ID to use as an authentication token for
     *       subsequent requests.
     * @param username  the username of the user
     * @param password  the password of the user
     */
    @Override
    public void login(String username, String password) throws IOException {
        var parameters = new User(username, password).toJson();
        Optional<String> response = sendRequest("login", parameters,
                "Login effettuato con successo",
                "Impossibile effettuare il login",
                false);
        if (response.isPresent()) {
            saveUserId(UUID.fromString(response.get()));
        }
    }

    /**
     * Send a login request to the server and wait for a response.
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     */
    @Override
    public void logout() {
        var parameters = new JSONObject();
        sendRequest("logout", parameters,
                "Logout effettuato con successo",
                "Impossibile effettuare il logout",
                true);
    }

    /**
     * Send a 'create document' request to the server
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     * @param document
     * @param sections
     */
    @Override
    public void create(String document, int sections) {
        Optional<UUID> userId = getSavedUserId();
        if (!userId.isPresent()) {
            System.out.println("Devi eseguire il login prima di poter effettuare un'operazione");
        }
        else {
            var doc = new Document(document, sections, userId.get());
            var parameters = doc.toJson();
            sendRequest("create", parameters,
                    "Documento creato con successo",
                    "Impossibile creare documento",
                    false);
        }
    }

    /**
     * Send a 'share document' request to the server
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     * @param document
     * @param username
     */
    @Override
    public void share(String document, String username) {
        var invite = new Invitation(document, username);
        var parameters = invite.toJson();
        sendRequest("share", parameters,
                "Documento condiviso con successo",
                "Impossibile condividere documento",
                true);
    }

    /**
     * Send a 'show section' request to the server
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     * @param documentName
     * @param section
     */
    @Override
    public void show(String documentName, int section) {
        var parameters = new JSONObject()
                .put("documentName", documentName)
                .put("section", section);
        Optional<String> response = sendRequest("show", parameters,
                "Contenuto della sezione " + section + ": ",
                "Impossibile visualizzare sezione",
                true);
        response.ifPresent(System.out::println);
    }

    /**
     * Send a 'show document' request to the server
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     * @param documentName
     */
    @Override
    public void show(String documentName) {
        var parameters = new JSONObject().put("documentName", documentName);
        Optional<String> response = sendRequest("show", parameters,
                "Contenuto del documento: ",
                "Impossibile visualizzare documento",
                true);
        response.ifPresent(System.out::println);
    }

    /**
     * Send a 'list documents' request to the server.
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     */
    @Override
    public void list() {
        var parameters = new JSONObject();
        Optional<String> response = sendRequest("list", parameters,
                "Documenti modificabili: ",
                "Impossibile visualizzare la lista dei documenti",
                true);
        response.ifPresent(System.out::println);
    }

    /**
     * Send an 'edit section' request to the server.
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     * @param document
     * @param section
     */
    @Override
    public void edit(String document, int section) {
        var parameters = new JSONObject()
                .put("documentName", document)
                .put("section", section);
        Optional<String> response = sendRequest("edit", parameters,
                "Sezione " + section + " del documento " + document + " scaricata con successo",
                "Impossibile modificare la sezione",
                true);
        response.ifPresent(System.out::println);
    }

    /**
     * Send an 'end edit section' request to the server.
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     * @param document
     * @param section
     * @param content
     */
    @Override
    public void endEdit(String document, int section, String content) {

    }
}
