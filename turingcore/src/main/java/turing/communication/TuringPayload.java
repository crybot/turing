package turing.communication;

import org.json.JSONObject;

public class TuringPayload implements Payload {
    private JSONObject payload;

    private TuringPayload(String payload) {
        this.payload = new JSONObject(payload);
    }
    private TuringPayload(JSONObject payload) {
        this.payload = payload;
    }

    public JSONObject getJson() {
        return payload;
    }

    @Override
    public String formatted() {
        return payload.toString();
    }

    public static TuringPayload of(JSONObject payload) {
        return new TuringPayload(payload);
    }

    public static TuringPayload of(String json) {
        return new TuringPayload(new JSONObject(json));
    }

    // public static TuringPayload makeResponse(String response) {
        // return new TuringPayload(new JSONObject().put("response", response));
    // }

}
