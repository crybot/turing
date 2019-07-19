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
public class UserDataManager extends DataManager<UUID, User> {

    public UserDataManager(String path) {
        super(path, "users", User.class);
    }

    public Optional<User> getByName(String name) {
        return getAll().stream().filter(user -> user.name.equals(name)).findFirst();
    }

    public Optional<User> getByNameAndPassword(String name, String password) {
        return getByName(name).filter(user -> user.password.equals(password));
    }

    @Override
    //TODO: implement
    public Optional<UUID> create(User entity) {
        return Optional.empty();
    }

    @Override
    //TODO: implement
    protected boolean contains(List<User> entities, User entity) {
        return false;
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
