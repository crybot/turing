package turing.client.io;

import org.json.JSONObject;
import turing.client.io.ClientUserInterface;
import turing.client.io.TuringParser;
import turing.communication.Message;
import turing.communication.TuringPayload;
import turing.communication.tcp.TcpCommunication;
import turing.communication.tcp.TcpMessage;
import turing.model.user.User;
import turing.util.stream.StreamUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Exposes and implements Client services for the Turing application
 */
public class TuringClientCommandLineInterface implements ClientUserInterface {
    private static final File cachedCredentials = new File("./cache/credentials");

    public TuringClientCommandLineInterface() { }

    /**
     * Store users's credentials in a persistent location for later work.
     * Stored credentials are transparently retrieved by the logout procedure.
     * Only one user at a time can store his credentials in the cached file,
     * although it is encoded in a for implementation reasons.
     * @param username  username to store
     * @param password  password to store
     * @throws IOException  if the credentials file does not exist and cannot be created
     */
    private void saveCredentials(String username, String password) throws IOException {
        List<User> users = List.of(new User(username, password));
        // Create credentials file (and its parent directories) if it does not exist
        if (!cachedCredentials.exists()) {
            cachedCredentials.getParentFile().mkdirs();
            Files.createFile(cachedCredentials.toPath());
        }
        StreamUtils.serializeEntities(users, "credentials", new FileOutputStream(cachedCredentials));
    }

    /**
     * Retrieve previously stored user's credentials
     * @return  user's credentials wrapped in an Optional.
     */
    private Optional<User> getSavedCredentials() {
        List<User> users;
        try {
            users = StreamUtils.deserializeEntities(cachedCredentials, "credentials", User.class);
            if (users != null && users.size() == 1) {
                return Optional.ofNullable(users.get(0));
            }
        }
        catch (IOException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public void register(String username, String password) {

    }

    /**
     * Send a login request to the server and wait for a response.
     * If any response is received, it will be printed on screen.
     * @param username  the username of the user
     * @param password  the password of the user
     */
    @Override
    public void login(String username, String password) {
        try {
            var communication = new TcpCommunication(InetAddress.getLocalHost(), 1024);
            var json = new JSONObject().put("login", new JSONObject()
                    .put("name", username)
                    .put("password", password));
            communication.sendMessage(TcpMessage.makeRequest(json));

            Optional<TcpMessage> response = communication.consumeMessage();
            // If any response has been received
            if (response.isPresent()) {
                // Print server response to screen if a "response" field is present
                response.get().getResponse().ifPresent(System.out::println);
                // If login successful: save user credentials for later work
                if (response.get().getOk()) {
                    saveCredentials(username, password);
                }
            }
        }
        catch (Exception e) {
            System.err.println("Errore: " + e.getLocalizedMessage());
        }
    }

    /**
     * Send a login request to the server and wait for a response.
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     */
    @Override
    public void logout() {
        Optional<User> credentials = getSavedCredentials();
        if (!credentials.isPresent()) {
            System.out.println("Devi aver prima eseguito l'accesso per poter effettuare il logout.");
        } else {
            try {
                var communication = new TcpCommunication(InetAddress.getLocalHost(), 1024);
                var json = new JSONObject().put("logout", new JSONObject()
                        .put("name", credentials.get().name)
                        .put("password", credentials.get().password));
                communication.sendMessage(TcpMessage.makeRequest(json));

                Optional<TcpMessage> response = communication.consumeMessage();
                // If any response has been received
                // Print server response to screen if a "response" field is present
                response.ifPresent(tcpMessage -> tcpMessage.getResponse().ifPresent(System.out::println));
            } catch (Exception e) {
                System.err.println("Errore: " + e.getLocalizedMessage());
            }
        }
    }


    @Override
    public void create(String document, int sections) {

    }

    @Override
    public void share(String document, String username) {

    }

    @Override
    public void show(String document, int section) {

    }

    @Override
    public void show(String document) {

    }

    @Override
    public void list() {

    }

    @Override
    public void edit(String documnet, int section) {

    }

    @Override
    public void endEdit(String document, int section) {

    }

    @Override
    public void printHelp() {

    }
}
