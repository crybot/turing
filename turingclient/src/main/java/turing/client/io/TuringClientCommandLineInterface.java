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

public class TuringClientCommandLineInterface implements ClientUserInterface {
    private TuringParser parser;
    private static final File cachedCredentials = new File("./cache/credentials");

    public TuringClientCommandLineInterface() {
        parser = new TuringParser();
    }

    private void saveCredentials(String username, String password) throws IOException {
        List<User> users = List.of(new User(username, password));
        // Create credentials file (and its parent directories) if it does not exist
        if (!cachedCredentials.exists()) {
            cachedCredentials.getParentFile().mkdirs();
            Files.createFile(cachedCredentials.toPath());
        }
        StreamUtils.serializeEntities(users, "credentials", new FileOutputStream(cachedCredentials));
    }

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

    @Override
    public void login(String username, String password) {
        try {
            //TODO: hide socket creation inside TcpCommunication
            var socket = new Socket(InetAddress.getLocalHost(), 1024);
            var communication = new TcpCommunication(socket);
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

    @Override
    public void logout() {
        Optional<User> credentials = getSavedCredentials();
        if (!credentials.isPresent()) {
            System.out.println("Devi aver prima eseguito l'accesso per poter effettuare il logout.");
        } else {
            try {
                //TODO: hide socket creation inside TcpCommunication
                var socket = new Socket(InetAddress.getLocalHost(), 1024);
                var communication = new TcpCommunication(socket);
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
