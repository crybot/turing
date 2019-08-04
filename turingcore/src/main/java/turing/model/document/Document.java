package turing.model.document;

import org.json.JSONObject;
import turing.model.Identifiable;
import turing.model.MapsJson;

import java.io.Serializable;
import java.util.*;

/**
 *
 */
public class Document implements Serializable, MapsJson, Identifiable<UUID> {
    private UUID id;
    private UUID authorId;
    private String name;
    private List<String> sections;

    public Document(String name, int sections, UUID authorId) {
        this(name, sections, authorId, UUID.randomUUID());
    }

    public Document(String name, int sections, UUID authorId, UUID id) {
        this.name = name;
        this.sections = new ArrayList<>((Collections.nCopies(sections, "")));
        this.authorId = authorId;
        this.id = id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getSection(int section) {
        try {
            return Optional.ofNullable(sections.get(section - 1));
        }
        catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return String.join(System.lineSeparator(), sections);
    }

    @Override
    public JSONObject toJson() {
        return new JSONObject()
                .put("name", name)
                .put("sections", sections)
                .put("authorId", authorId)
                .put("id", id.toString());
    }
}
