package turing.communication;

import org.json.JSONObject;

public class JsonPayload implements Payload {
    private JSONObject payload;

    private JsonPayload(String payload) {
        this.payload = new JSONObject(payload);
    }
    private JsonPayload(JSONObject payload) {
        this.payload = payload;
    }

    public JSONObject getJson() {
        return payload;
    }

    @Override
    public String formatted() {
        return payload.toString();
    }

    public static JsonPayload of(JSONObject payload) {
        return new JsonPayload(payload);
    }

    public static JsonPayload of(String json) {
        return new JsonPayload(new JSONObject(json));
    }

    // public static JsonPayload makeResponse(String response) {
        // return new JsonPayload(new JSONObject().put("response", response));
    // }

}
