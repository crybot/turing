package turing.communication.tcp;

import org.json.JSONObject;
import turing.communication.JsonPayload;
import turing.communication.Message;

import java.util.Optional;

public class TcpMessage extends Message<JsonPayload> {
    public TcpMessage(JsonPayload content) {
        super(content);
    }

    public static TcpMessage makeRequest(JSONObject json) {
        return new TcpMessage(JsonPayload.of(json));
    }
    public static TcpMessage makeResponse(JSONObject json) {
        String response = json.has("response") ? json.getString("response") : "";
        boolean ok = !json.has("ok") || json.getBoolean("ok");
        return TcpMessage.makeResponse(response, ok);
    }
    public static TcpMessage makeResponse(String response, boolean ok) {
        JSONObject payload = new JSONObject();
        payload.put("response", response);
        payload.put("ok", ok);
        return new TcpMessage(JsonPayload.of(payload));
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
}
