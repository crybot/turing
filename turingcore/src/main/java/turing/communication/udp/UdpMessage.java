package turing.communication.udp;

import org.json.JSONObject;
import turing.communication.JsonPayload;
import turing.communication.Message;
import turing.communication.tcp.TcpMessage;

public class UdpMessage extends TcpMessage {
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
}
