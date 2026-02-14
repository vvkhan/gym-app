package com.epam.gym.core.service;

import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Abstract base service with common structure for service operations (Template Method pattern).
 */
public abstract class AbstractService<T, ID> {

    protected T createEntity(T entity) {
        getLogger().debug("Creating {}: {}", getEntityName(), entity);

        T created = doCreate(entity);

        getLogger().info("Created {} with id: {}", getEntityName(), getEntityId(created));
        return created;
    }

    protected T updateEntity(ID id, T entity) {
        getLogger().debug("Updating {} with id: {}", getEntityName(), id);

        T updated = doUpdate(id, entity);

        getLogger().info("Updated {} with id: {}", getEntityName(), id);
        return updated;
    }

    protected void deleteEntity(ID id) {
        getLogger().debug("Deleting {} with id: {}", getEntityName(), id);

        doDelete(id);

        getLogger().info("Deleted {} with id: {}", getEntityName(), id);
    }

    protected T getEntity(ID id) {
        getLogger().debug("Getting {} by id: {}", getEntityName(), id);
        return doGet(id);
    }

    // Common getter methods

    protected Optional<T> getById(ID id) {
        getLogger().debug("Getting {} by id: {}", getEntityName(), id);
        return daoFindById(id);
    }

    protected List<T> getAll() {
        getLogger().debug("Getting all {}s", getEntityName());
        return daoFindAll();
    }

    protected Optional<T> getByUsername(String username) {
        getLogger().debug("Getting {} by username: {}", getEntityName(), username);
        return daoFindByUsername(username);
    }

    // Abstract methods

    protected abstract Logger getLogger();

    protected abstract String getEntityName();

    protected abstract ID getEntityId(T entity);

    protected abstract T doCreate(T entity);

    protected abstract T doUpdate(ID id, T entity);

    protected abstract void doDelete(ID id);

    protected abstract T doGet(ID id);

    // DAO common methods

    protected abstract Optional<T> daoFindById(ID id);

    protected abstract List<T> daoFindAll();

    protected Optional<T> daoFindByUsername(String username) {
        throw new UnsupportedOperationException("Username lookup not supported for " + getEntityName());
    }

}
