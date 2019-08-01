package turing.server.persistence;

import turing.model.document.Document;
import turing.model.user.User;
import turing.util.stream.StreamUtils;

import javax.print.Doc;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

//TODO: handle synchronization
public class DocumentDataManager extends DataManager<UUID, Document> {

    public DocumentDataManager(String path) {
        super(path, "documents", Document.class);
    }

    public Optional<Document> getByName(String name) {
        return getAll().stream().filter(doc -> doc.getName().equals(name)).findFirst();
    }

    public List<Document> getByAuthor(User author) {
        return getAll().stream().filter(doc -> doc.getAuthorId().equals(author.id)).collect(Collectors.toList());
    }

    public Optional<Document> getByNameAndAuthor(String name, User author) {
        return getByName(name).filter(doc -> doc.getAuthorId().equals(author.id));
    }

    @Override
    protected boolean contains(List<Document> documents, Document document) {
        return documents.stream().anyMatch(doc -> doc.getName().equals(document.getName()));
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
