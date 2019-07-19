package turing.communication;

import org.json.JSONObject;

public class JsonPaylod implements Payload {
    private JSONObject payload;

    private JsonPaylod(String payload) {
        this.payload = new JSONObject(payload);
    }
    private JsonPaylod(JSONObject payload) {
        this.payload = payload;
    }

    public JSONObject getJson() {
        return payload;
    }

    @Override
    public String formatted() {
        return payload.toString();
    }

    public static JsonPaylod of(JSONObject payload) {
        return new JsonPaylod(payload);
    }

    public static JsonPaylod of(String json) {
        return new JsonPaylod(new JSONObject(json));
    }

    // public static JsonPaylod makeResponse(String response) {
        // return new JsonPaylod(new JSONObject().put("response", response));
    // }

}
