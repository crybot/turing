package turing.util.stream;

import org.json.JSONArray;
import org.json.JSONObject;
import turing.model.JsonMapper;
import turing.model.MapsJson;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class StreamUtils {

    //TODO: handle file locking
    public static <T extends MapsJson> List<T> deserializeEntities(File file, String root, Class<T> tClass) throws IOException {
        var channel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
        // Memory mapped file
        var buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());

        // If the file is empty => return an empty list
        if (!buffer.hasRemaining()) {
            return new ArrayList<>();
        }
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
        //TODO: close channel
        return entities;
    }

    public static <T extends MapsJson> void serializeEntities(List<T> entities, String root, FileOutputStream file) throws IOException {
        var channel = file.getChannel();

        List<JSONObject> jsonObjects = entities.stream()
                .map(MapsJson::toJson)
                .collect(Collectors.toList());
        JSONObject serialized = new JSONObject().put(root, jsonObjects);

        channel.write(ByteBuffer.wrap(serialized.toString().getBytes()));
        channel.close();
    }
}
