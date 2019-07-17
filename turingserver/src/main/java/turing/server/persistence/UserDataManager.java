package turing.server.persistence;

import turing.model.user.User;
import turing.util.stream.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDataManager implements DataManager<User> {
    private File getUsersFile() {
        // NIO needs a FileChannel => I have to retrieve a File handle => I can't retrieve a File handle from the classpath
        // => the file is placed outsite the classpath and therefore I use the relative path from the root of the
        // project
        return new File("./model/user/users");
        // return new File(getClass().getResource("/model/user/users").getPath());
        // return new File(Thread.currentThread().getContextClassLoader().getResource("model/user/users").getPath());
        // return new File(ClassLoader.getSystemResource("classpath:model/user/users").getPath());
    }

    @Override
    public Optional<User> get(UUID id) {
        return Optional.empty();
    }

    public Optional<User> getByName(String name) {
        return getAll().stream().filter(user -> user.name.equals(name)).findFirst();
    }

    public Optional<User> getByNameAndPassword(String name, String password) {
        return getByName(name).filter(user -> user.password.equals(password));
    }

    @Override
    public List<User> getAll() {
        try {
            return StreamUtils.deserializeEntities(getUsersFile(), "users", User.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Optional<UUID> create(User entity) {
        return Optional.empty();
    }

    @Override
    public boolean update(User entity) {
        return false;
    }

    @Override
    public boolean delete(User entity) {
        return false;
    }
}
