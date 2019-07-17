package turing.communication;

import org.json.JSONObject;

public class TuringPayload implements Payload {
    private JSONObject payload;

    public TuringPayload(String payload) {
        this.payload = new JSONObject(payload);
    }
    public TuringPayload(JSONObject payload) {
        this.payload = payload;
    }

    public JSONObject getJson() {
        return payload;
    }

    @Override
    public String formatted() {
        return payload.toString();
    }

    public static TuringPayload makeResponse(String response) {
        return new TuringPayload(new JSONObject().put("response", response));
    }

}
