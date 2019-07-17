package turing.communication.tcp;

import turing.communication.Communication;
import turing.communication.CommunicationManager;
import turing.communication.TuringPayload;

import java.io.IOException;
import java.net.ServerSocket;

public class TcpCommunicationManager extends CommunicationManager {

    //TODO: Implement
    private ServerSocket serverSocket;

    public void setup(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public Communication<TuringPayload> acceptCommunication() {
        try {
            return new TcpCommunication(serverSocket.accept());
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }
}
