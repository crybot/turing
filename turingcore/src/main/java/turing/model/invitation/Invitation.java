package turing.model.invitation;

import org.json.JSONObject;
import turing.model.Identifiable;
import turing.model.MapsJson;

import java.io.Serializable;
import java.util.UUID;

/**
 * An Invitation models the ability of an user to edit a given document
 * An Invitation is immutable.
 */
public class Invitation implements Serializable, MapsJson, Identifiable<UUID> {
    public final String documentName;
    public final String userName;
    public final UUID id;

    public Invitation(final String documentName, final String userName, UUID id) {
        this.documentName = documentName;
        this.userName = userName;
        this.id = id;
    }

    public Invitation(final String documentName, final String userName) {
        this(documentName, userName, UUID.randomUUID());
    }

    @Override
    public JSONObject toJson() {
        return new JSONObject()
                .put("documentName", documentName)
                .put("userName", userName)
                .put("id", id.toString());

    }

    @Override
    public UUID getId() {
        return id;
    }
}
