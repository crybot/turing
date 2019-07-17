import turing.model.user.User;
import turing.util.stream.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class StreamUtilsTest {
    private static final File cachedCredentials = new File("./cache/credentials");

    private static void saveCredentials(String username, String password) throws IOException {
        List<User> users = List.of(new User(username, password));
        if (!cachedCredentials.exists()) {
            cachedCredentials.getParentFile().mkdirs();
            Files.createFile(cachedCredentials.toPath());
        }
        StreamUtils.serializeEntities(users, "credentials", new FileOutputStream(cachedCredentials));
        System.out.println(StreamUtils.deserializeEntities(cachedCredentials, "credentials", User.class));
    }

    public static void main(String[] args) throws IOException {
        // saveCredentials("foo", "bar");
    }
}
