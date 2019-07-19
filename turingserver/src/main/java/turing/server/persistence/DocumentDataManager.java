package turing.server.persistence;

import turing.model.document.Document;
import turing.model.user.User;
import turing.util.stream.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//TODO: handle synchronization
public class DocumentDataManager implements DataManager<Document> {

    private File getDocumentsFile() throws IOException {
        return getFile("./model/document/documents");
    }

    @Override
    public Optional<Document> get(UUID id) {
        return getAll().stream().filter(doc -> doc.getId().equals(id)).findFirst();
    }

    public Optional<Document> getByName(String name) {
        return getAll().stream().filter(doc -> doc.getName().equals(name)).findFirst();
    }

    public Optional<Document> getByNameAndAuthor(String name, User author) {
        return getByName(name).filter(doc -> doc.getAuthorId().equals(author.id));
    }

    @Override
    public List<Document> getAll() {
        try {
            return StreamUtils.deserializeEntities(getDocumentsFile(), "documents", Document.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Optional<UUID> create(Document entity) {
        if (entity == null || entity.getId() == null) {
            return Optional.empty();
        }

        List<Document> documents = getAll();
        // If there is already a document with the same name
        if (documents.stream().anyMatch(doc -> doc.getName().equals(entity.getName()))) {
            return Optional.empty();
        }

        documents.add(entity);
        try {
            StreamUtils.serializeEntities(documents, "documents", new FileOutputStream(getDocumentsFile()));
            return Optional.ofNullable(entity.getId());
        }
        catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    //TODO: implement
    public boolean update(Document entity) {
        return false;
    }

    @Override
    //TODO: implement
    public boolean delete(Document entity) {
        return false;
    }
}
