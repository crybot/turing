package turing.client;

import org.json.JSONObject;
import turing.client.io.TuringParser;
import turing.communication.Message;
import turing.communication.TuringPayload;
import turing.communication.tcp.TcpCommunication;
import java.net.InetAddress;
import java.net.Socket;

public class TuringClientCommandLineInterface implements ClientUserInterface {
    private TuringParser parser;

    public TuringClientCommandLineInterface() {
        parser = new TuringParser();
    }

    @Override
    public void register(String username, String password) {

    }

    @Override
    public void login(String username, String password) {
        try {
            var socket = new Socket(InetAddress.getLocalHost(), 1024);
            var communication = new TcpCommunication(socket);
            var json = new JSONObject().put("login", new JSONObject()
                    .put("user", username)
                    .put("password", password));
            communication.sendMessage(new Message<>(new TuringPayload(json)));

            var response = communication.consumeMessage();
            response.ifPresent(r -> System.out.println(r.getContent().formatted()));

            // var socket = new Socket("localhost", 1024);
            // var out = new JsonStreamWriter(socket.getOutputStream());
            // var message1 = new JSONObject().put("login", new JSONObject()
            //         .put("user", username)
            //         .put("password", password));

            // out.write(message1);
            // out.newLine();
            // out.flush();
            // socket.shutdownOutput();

            // var in = new JsonStreamReader(socket.getInputStream());
            // System.out.println(in.readJson());
            // in.close();
        }
        catch (Exception e) {
            System.err.println("Connection error: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void logout() {

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
