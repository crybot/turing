package turing.server.persistence;

import turing.model.Identifiable;
import turing.model.MapsJson;
import turing.util.stream.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Define CRUD operations on persistent data-types
 * @param <K>   Type of objects that identify objects of type T
 * @param <T>   Type of the persistent entities (with a 1-1 Json mapping)
 */
public abstract class DataManager<K, T extends Identifiable<K> & MapsJson> {
    private String path;
    private String collectionName;
    private Class<T> typeClass;

    protected DataManager(String path, String collectionName, Class<T> typeClass) {
        this.path = path;
        this.collectionName = collectionName;
        this.typeClass = typeClass;
    }
    /**
     * Get the file containing the persistent entities.
     * If the file does not exist, it gets created, along with its full path.
     * @return
     * @throws IOException
     */
    protected File getFile() throws IOException {
        // NIO needs a FileChannel => I have to retrieve a File handle => I can't retrieve a File handle from the classpath
        // => the file is placed outsite the classpath and therefore I use the relative path from the root of the
        // project
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            Files.createFile(file.toPath());
        }
        return file;
    }

    /**
     * Get the entity with the specified id
     * @param id    The id of the entity to collect
     * @return
     */
    public Optional<T> get(K id) {
        return getAll().stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    /**
     * Get all entities currently stored in the persistent layer.
     * Notes: to deserialize the entity we need to pass the typeclass of T, which does not exist due to Type erasure
     * (T does not get reified at runtime).
     * The workaround consists in passing the typeclass to the superclass' constructor of the extending classes.
     * @return  A List of all the persistent entities of type T
     */
    public List<T> getAll() {
        try {
            return StreamUtils.deserializeEntities(getFile(), collectionName, typeClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Optional<K> create(T entity) {
        if (entity == null || entity.getId() == null) {
            return Optional.empty();
        }

        List<T> entities = getAll();

        //TODO: T could override equals and the contains might be completely specified
        //      inside the abstract class
        if (contains(entities, entity)) {
            return Optional.empty();
        }

        entities.add(entity);
        try {
            StreamUtils.serializeEntities(entities, collectionName, new FileOutputStream(getFile()));
            return Optional.of(entity.getId());
        }
        catch (IOException e) {
            return Optional.empty();
        }

    }

    protected abstract boolean contains(List<T> entities, T entity);
    public abstract boolean update(T entity);
    public abstract boolean delete(T entity);
}
