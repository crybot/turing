package turing.server;

import jdk.jshell.spi.ExecutionControl;
import org.json.JSONObject;
import turing.communication.Communication;
import turing.communication.Message;
import turing.communication.Payload;
import turing.communication.TuringPayload;
import turing.communication.tcp.TcpCommunicationManager;
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
            Optional<Message<TuringPayload>> message = communication.consumeMessage();
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
        JSONObject json = message.getContent().getJson();
        ServerLogic serverLogic = new ServerLogic(serverState, communication);
        if (json.has("login")) {
            var login = json.getJSONObject("login");
            serverLogic.login(login.getString("user"), login.getString("password"));
        }
    }

    public void close() throws IOException {
    }
}
