package turing.communication.json;

import org.json.JSONObject;

import java.io.*;

public class JsonStreamWriter extends BufferedWriter {

    public JsonStreamWriter(Writer out) {
        super(out);
    }

    public JsonStreamWriter(OutputStream out) {
        super(new OutputStreamWriter(out));
    }

    public void write(JSONObject json) throws IOException {
        write(json.toString());
    }
}
