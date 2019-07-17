package turing.util.stream;

import jdk.jshell.spi.ExecutionControl;
import org.json.JSONArray;
import org.json.JSONObject;
import turing.model.JsonMapper;
import turing.model.MapsJson;
import turing.model.user.User;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public abstract class StreamUtils {

    //TODO: handle file locking
    public static <T extends MapsJson> List<T> deserializeEntities(File file, String root, Class<T> tClass) throws IOException {
        var channel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
        // Memory mapped file
        var buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());

        // Decode the bytebuffer with the default charset
        Charset charset = Charset.defaultCharset();
        CharsetDecoder decoder = charset.newDecoder();

        JSONObject json = new JSONObject(decoder.decode(buffer).toString());
        JSONArray jsonArray = json.getJSONArray(root);

        List<T> entities = new ArrayList<>();
        for (int i=0; i<jsonArray.length(); i++) {
            try {
                entities.add(JsonMapper.fromJson(jsonArray.getJSONObject(i), tClass));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return entities;
    }

    public static <T> void serializeEntities(List<T> entities, InputStream in) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("Not implemented yet");
    }
}
