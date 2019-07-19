package turing.client.io;

import org.json.JSONObject;
import turing.communication.tcp.TcpCommunication;
import turing.communication.tcp.TcpMessage;
import turing.model.document.Document;
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
    // private static final File cachedCredentials = new File("./cache/credentials");
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
            var json = new JSONObject().put("login", new User(username, password).toJson());
            communication.sendMessage(TcpMessage.makeRequest(json));

            Optional<TcpMessage> response = communication.consumeMessage();
            // If any response has been received
            if (response.isPresent()) {
                // If the response contains any content
                var content = response.get().getResponse();
                if (content.isPresent()) {
                    // If the login is successful the serve returns the UUID of the logged user
                    String uuid = content.get();
                    // If login successful: save the returned userId for later work
                    if (response.get().getOk()) {
                        saveUserId(UUID.fromString(uuid));
                        System.out.println("Login effettuato con successo: " + content.get());
                    }
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
        Optional<UUID> userId = getSavedUserId();
        if (!userId.isPresent()) {
            System.out.println("Devi aver prima eseguito l'accesso per poter effettuare il logout.");
        } else {
            try {
                var communication = new TcpCommunication(InetAddress.getLocalHost(), 1024);
                var json = new JSONObject().put("logout", new JSONObject().put("userId", userId.get()));
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
        Optional<UUID> userId = getSavedUserId();
        if (!userId.isPresent()) {
            System.out.println("Devi eseguire il login prima di poter effettuare un'operazione");
        }
        else {
            try {
                var communication = new TcpCommunication(InetAddress.getLocalHost(), 1024);
                var doc = new Document(document, sections, userId.get());
                var json = new JSONObject().put("create", doc.toJson());
                communication.sendMessage(TcpMessage.makeRequest(json));

                Optional<TcpMessage> response = communication.consumeMessage();
                // If any response has been received
                // Print server response to screen if a "response" field is present
                response.ifPresent(tcpMessage -> tcpMessage.getResponse().ifPresent(System.out::println));
            }
            catch (Exception e) {
                System.err.println("Errore: " + e.getLocalizedMessage());
            }
        }
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
    public void edit(String document, int section) {

    }

    @Override
    public void endEdit(String document, int section) {

    }

    @Override
    public void printHelp() {

    }
}
