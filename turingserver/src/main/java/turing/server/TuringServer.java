package turing.server;

import turing.communication.rmi.RegistrationService;
import turing.communication.tcp.TcpMessage;
import turing.communication.udp.UdpCommunication;
import turing.communication.udp.UdpMessage;
import turing.server.communication.rmi.RemoteRegistrationService;
import turing.server.communication.tcp.TcpCommunicationManager;
import turing.server.state.ServerState;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
     * Expose to the clients a registration service remotely invokable
     * @throws RemoteException
     */
    private void publishRegistrationService() throws RemoteException {
        RemoteRegistrationService registrationService = new RemoteRegistrationService(serverState);
        LocateRegistry.createRegistry(RegistrationService.REGISTRY_PORT);
        Registry registry = LocateRegistry.getRegistry(RegistrationService.REGISTRY_PORT);
        registry.rebind(RegistrationService.SERVICE_NAME, registrationService);
    }

    /**
     * Start the server and setup the communication managers
     * @throws IOException
     */
    //TODO: add read and write timeouts
    public void start() throws IOException {
        tcpManager.setup(port);
        publishRegistrationService();

        /* UDP Request manager thread */
        threadPool.submit(() -> {
            try {
                var communication = UdpCommunication.accept(8192);
                while (true) {
                    Optional<UdpMessage> message = communication.consumeMessage();
                    message.ifPresent(udpMessage -> threadPool.submit(() ->
                            new ServerLogic(serverState, communication).decodeAndExecute(udpMessage, communication)
                    ));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        /*----------------------------*/

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
