package turing.server.persistence;

import turing.model.user.User;
import turing.util.stream.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//TODO: handle synchronization
public class UserDataManager implements DataManager<User> {

    private File getUsersFile() throws IOException {
        return getFile("./model/user/users");
    }

    @Override
    public Optional<User> get(UUID id) {
        return getAll().stream().filter(user -> user.id.equals(id)).findFirst();
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
    //TODO: implement
    public Optional<UUID> create(User entity) {
        return Optional.empty();
    }

    @Override
    //TODO: implement
    public boolean update(User entity) {
        return false;
    }

    @Override
    //TODO: implement
    public boolean delete(User entity) {
        return false;
    }
}
