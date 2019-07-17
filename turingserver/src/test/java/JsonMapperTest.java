import org.json.JSONObject;
import turing.model.JsonMapper;
import turing.model.user.User;
import turing.server.persistence.UserDataManager;
import turing.util.stream.StreamUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class JsonMapperTest {
    public static void main (String[] args) throws IllegalAccessException, InstantiationException, InvocationTargetException, IOException {

        // JsonMapper.fromJson(json, User.class);
        User user = new User("user", "password");
        System.out.println("Before serialization:");
        System.out.println("User: " + user.name);
        System.out.println("Password: " + user.password);
        System.out.println("ID: " + user.id);

        JSONObject json = new JSONObject()
                .put("name", user.name)
                .put("password", user.password)
                .put("id", user.id);

        User newUser = JsonMapper.fromJson(json, User.class);

        System.out.println("After serialization:");
        System.out.println("User: " + newUser.name);
        System.out.println("Password: " + newUser.password);
        System.out.println("ID: " + newUser.id);

        System.out.println();

        // IO file read
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("model/user/users").getPath());
        var reader = new BufferedReader(new FileReader(file));
        reader.lines().forEach(System.out::println);

        System.out.println();

        var dataManager = new UserDataManager();

        // NIO file read
        List<User> users = dataManager.getAll();
        users.forEach(u -> System.out.println(u.name + " - " + u.password + " - " + u.id));

    }
}
