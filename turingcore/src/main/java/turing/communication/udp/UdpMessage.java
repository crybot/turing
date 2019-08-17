package turing.communication.udp;

import org.json.JSONObject;
import turing.communication.JsonPayload;
import turing.communication.Message;
import turing.communication.tcp.TcpMessage;

import java.net.SocketAddress;
import java.util.Optional;

//TODO: common boilerplate with TcpMessage, try to factorize
public class UdpMessage extends Message<JsonPayload> {
    private SocketAddress address;

    public UdpMessage(JsonPayload content) {
        super(content);
    }

    public static UdpMessage makeResponse(JSONObject json) {
        String response = json.has("response") ? json.getString("response") : "";
        boolean ok = !json.has("ok") || json.getBoolean("ok");
        return UdpMessage.makeResponse(response, ok);
    }
    public static UdpMessage makeResponse(String response, boolean ok) {
        JSONObject payload = new JSONObject();
        payload.put("response", response);
        payload.put("ok", ok);
        return new UdpMessage(JsonPayload.of(payload));
    }
    public static UdpMessage makeResponse(byte[] data) {
        return UdpMessage.makeResponse(new JSONObject(new String(data)));
    }
    public static UdpMessage makeRequest(JSONObject json, SocketAddress address) {
        var message = new UdpMessage(JsonPayload.of(json));
        message.address = address;
        return message;
    }

    public Optional<String> getResponse() {
        if (getContent().getJson().has("response")) {
            return Optional.ofNullable(getContent().getJson().getString("response"));
        }
        return Optional.empty();
    }

    public boolean getOk() {
        if (getContent().getJson().has("ok")) {
            return getContent().getJson().getBoolean("ok");
        }
        return true; // Should we return false by default?
    }

    public SocketAddress getAddress() {
        return address;
    }
}
