package turing.communication.udp;

import org.json.JSONObject;
import turing.communication.*;
import turing.communication.json.JsonStreamReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.util.Optional;

public class UdpCommunication implements Communication<JsonPayload> {
    private DatagramChannel inputChannel;
    private DatagramChannel outputChannel;
    private SocketAddress remoteAddress;

    private UdpCommunication(int port) throws IOException {
        inputChannel = DatagramChannel.open();
        inputChannel.bind(new InetSocketAddress(port));

    }

    public static UdpCommunication accept(int port) throws IOException {
        return new UdpCommunication(port);
    }

    @Override
    public Optional<UdpMessage> consumeMessage() throws IOException {
        var buffer = ByteBuffer.allocateDirect(8192);

        if ((remoteAddress = inputChannel.receive(buffer)) != null) {
            buffer.flip();
            var bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            var reader = new JsonStreamReader(new ByteArrayInputStream(bytes));
            var json = reader.readJson();
            if (json.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new UdpMessage(JsonPayload.of(json)));
        }
        return Optional.empty();
    }

    @Override
    public void sendMessage(Message<JsonPayload> message) throws IOException {
        inputChannel.send(ByteBuffer.wrap(message.getContent().bytes()), remoteAddress);
    }

    @Override
    public void trySendMessage(Message<JsonPayload> message) {

    }

    @Override
    public void close() throws IOException {
        //TODO: close sockets
    }
}
