package turing.server;

import org.json.JSONObject;
import turing.communication.Communication;
import turing.communication.Message;
import turing.communication.TuringPayload;
import turing.communication.tcp.TcpMessage;
import turing.server.communication.tcp.TcpCommunicationManager;
import turing.server.state.ServerState;

import java.io.IOException;
import java.util.Optional;

/**
 * Turing's server interface implementation
 */
public class TuringServer implements ServerInterface {
    private final int port;
    private ServerState serverState;
    private TcpCommunicationManager tcpManager;

    public TuringServer(int port) {
        this.port = port;
        serverState = new ServerState();
        tcpManager = new TcpCommunicationManager();
    }

    /**
     * Start the server and setup the communication managers
     * @throws IOException
     */
    //TODO: add read and write timeouts
    public void start() throws IOException {
        tcpManager.setup(port);
        while (true) {
            var communication = tcpManager.acceptCommunication();
            Optional<TcpMessage> message = communication.consumeMessage();
            if (message.isPresent()) {
                try {
                    decodeAndExecute(message.get(), communication);
                }
                catch (Exception e) {
                    System.err.println("Could not process request: " + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Decode a given message into a meaningful operation and execute it
     * @param message   the request message
     * @param communication the open communication on which to send a possible response
     */
    private void decodeAndExecute(Message<TuringPayload> message,
                                  Communication<TuringPayload> communication) throws IOException {
        System.out.println("Decoding message: " + message.getContent().formatted());
        JSONObject json = message.getContent().getJson();
        ServerLogic serverLogic = new ServerLogic(serverState, communication);
        if (json.has("login")) {
            var user = json.getJSONObject("login");
            serverLogic.login(user.getString("name"), user.getString("password"));
        }
        else if (json.has("logout")) {
            var user = json.getJSONObject("logout");
            serverLogic.logout(user.getString("name"), user.getString("password"));
        }
    }

    public void close() throws IOException {
    }
}
