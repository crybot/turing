package turing.model.user;

import org.json.JSONObject;
import turing.model.Identifiable;
import turing.model.MapsJson;

import java.io.Serializable;
import java.util.UUID;

/**
 * Immutable data-model representation of an User
 */
public class User implements Serializable, MapsJson, Identifiable<UUID> {

    public final UUID id;
    public final String name;

    /**
     * Might become an Hash. For now we don't care about security/privacy
     */
    public final String password;

    public User(final String name, final String password) {
        this(name, password, UUID.randomUUID());
    }

    public User(final String name, final String password, final UUID id) {
        this.name = name;
        this.password = password;
        this.id = id;
    }

    @Override
    public JSONObject toJson() {
        return new JSONObject().put("name", name).put("password", password).put("id", id.toString());
    }

    @Override
    public String toString() {
        return "{ name=" + name + " password=" + password + " id=" + id + " }";
    }

    @Override
    public UUID getId() {
        return id;
    }
}
