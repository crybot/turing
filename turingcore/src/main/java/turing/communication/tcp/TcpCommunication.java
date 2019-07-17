package turing.communication.tcp;

import turing.communication.Communication;
import turing.communication.Message;
import turing.communication.TuringPayload;
import turing.communication.json.JsonStreamReader;
import turing.communication.json.JsonStreamWriter;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

public class TcpCommunication implements Communication<TuringPayload> {
    private Socket socket;
    private JsonStreamReader intputMessageStream;
    private JsonStreamWriter outputMessageStream;

    public TcpCommunication(Socket socket) throws IOException {
        this.socket = socket;
        intputMessageStream = new JsonStreamReader(socket.getInputStream());
        outputMessageStream = new JsonStreamWriter(socket.getOutputStream());
    }

    /**
     * Synchronously retrieve a message from the opened stream
     * @return The deserialized payload wrapped into an Optional (of a Message<Payload>)
     */
    @Override
    public Optional<Message<TuringPayload>> consumeMessage() throws IOException {
        System.out.println("Trying to consume a message...");
        var json = intputMessageStream.readJson();

        socket.shutdownInput();
        if (json.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Message<>(new TuringPayload(json)));
    }

    /**
     * Synchronously send a message to the output stream of the connection socket
     * @param message   The message to be sent
     */
    @Override
    public void sendMessage(Message<TuringPayload> message) throws IOException {
        System.out.println("Trying to send a message...");
        outputMessageStream.write(message.getContent().getJson());
        outputMessageStream.newLine();
        outputMessageStream.flush();
        socket.shutdownOutput();
    }

    /**
     * Close the message stream and the connection socket
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        intputMessageStream.close();
        outputMessageStream.close();
        socket.close();
    }
}
