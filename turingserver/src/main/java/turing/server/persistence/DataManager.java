package turing.server.persistence;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Define CRUD operations on persistent data-types
 * @param <T>
 */
public interface DataManager<T extends Serializable> {

    /**
     * Default method automatically available to all implementations
     * @param path
     * @return
     * @throws IOException
     */
    default File getFile(String path) throws IOException {
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

    Optional<T> get(UUID id);
    List<T> getAll();
    Optional<UUID> create(T entity);
    boolean update(T entity);
    boolean delete(T entity);
}
