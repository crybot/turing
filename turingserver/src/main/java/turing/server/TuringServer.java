package turing.server;

import turing.communication.tcp.TcpMessage;
import turing.server.communication.tcp.TcpCommunicationManager;
import turing.server.state.ServerState;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Turing's server interface implementation
 */
public class TuringServer implements ServerInterface {
    private final int port;
    private ServerState serverState;
    private TcpCommunicationManager tcpManager;
    private ExecutorService threadPool;

    public TuringServer(int port) {
        this.port = port;
        serverState = new ServerState();
        tcpManager = new TcpCommunicationManager();
        threadPool = Executors.newFixedThreadPool(4);
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

            // By submitting an anonymous Runnable object we obtain access
            // to TuringServer's global state
            message.ifPresent(tcpMessage -> threadPool.submit(() ->
                    new ServerLogic(serverState, communication).decodeAndExecute(tcpMessage, communication)
            ));
        }
    }

    public void close() throws IOException {
        //TODO: implement closing hook
    }
}
