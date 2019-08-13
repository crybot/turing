package turing.server.persistence;

import turing.model.user.User;
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
    protected boolean contains(List<User> entities, User entity) {
        return entities.stream().anyMatch(u -> u.name.equals(entity.name));
    }

    @Override
    //TODO: implement
    public boolean delete(User entity) {
        return false;
    }
}
