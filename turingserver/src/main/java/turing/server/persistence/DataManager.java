package turing.server.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Define CRUD operations on persistent data-types
 * @param <T>
 */
public interface DataManager<T extends Serializable> {
    Optional<T> get(UUID id);
    List<T> getAll();
    Optional<UUID> create(T entity);
    boolean update(T entity);
    boolean delete(T entity);
}
