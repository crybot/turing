package turing.communication.json;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class JsonStreamReader extends BufferedReader {
    public JsonStreamReader(Reader in) {
        super(in);
    }

    public JsonStreamReader(InputStream in) {
        super(new InputStreamReader(in));
    }

    /**
     * Reads a Json object from the stream if one is available.
     * @return JSONObject
     */
    public JSONObject readJson() {
        var json = new StringBuilder();
        try {
            lines().forEach(line -> json.append(line).append(System.lineSeparator()));
            return new JSONObject(json.toString());
        }
        catch (JSONException e) {
            return new JSONObject();
        }
    }
}
