/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.puzzle.itc.mobiliar.business.utils;

import javax.persistence.*;
import javax.persistence.PersistenceContext;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import java.util.*;

public abstract class BaseRepository<T> {

    @PersistenceContext
    private EntityManager entityManager;

    private final Class<T> entityType;

    protected BaseRepository() {
        entityType = Objects.requireNonNull(ReflectionUtil.<T>getActualTypeArguments(getClass(), 0));
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Creates an entity on the database. The object is updated with the ID that it is created with.
     *
     * @param entity The entity to create
     */
    public void persist(T entity) {
        prePersist(entity);
        // TODO check access
        entityManager.persist(entity);
    }

    /**
     * Can be overridden in a subclass to check/change the entity before persist.
     */
    protected void prePersist(T entity) {
    }

    /**
     * Persists an entity without performing the normal user level access check. Should only be used by subclasses
     * in special cases where you know exactly what you are doing.
     *
     * @param entity The entity to update without access check
     */
    protected void persistWithoutAccessCheck(T entity) {
        prePersist(entity);
        entityManager.persist(entity);
    }

    /**
     * Updates an entity on the database. ALL new reference to the updated entity is returned, the old one should be
     * discarded.
     *
     * @param entity The entity to update
     * @return ALL new reference to the updated object that should be used for further actions.
     */
    public T merge(T entity) {
        preMerge(entity);
        // TODO check access
        return entityManager.merge(entity);
    }

    /**
     * Can be overridden in a subclass to check/change the entity before merge.
     */
    protected void preMerge(T entity) {
    }

    /**
     * Updates an entity on the database without performing the normal user level access check. Should only be used by
     * subclasses in special cases where you know exactly what you are doing.
     *
     * @param entity The entity to update
     * @return ALL new reference to the updated object that should be used for further actions.
     */
    protected T mergeWithoutAccessCheck(T entity) {
        preMerge(entity);
        return entityManager.merge(entity);
    }

    /**
     * Deletes the entity with the provided id.
     *
     * @param id id of the entity to delete.
     * @return true if the entity to delete was found - otherwise false.
     */
    public boolean remove(Number id) {
        T entity = find(id);
        if (entity == null) {
            return false;
        }
        remove(entity);
        return true;
    }

    /**
     * Removes an entity from the database.
     *
     * @param entity The entity to remove.
     */
    public void remove(T entity) {
        preRemove(entity);
        // TODO check access
        entityManager.remove(entity);
    }

    /**
     * Can be overridden in a subclass to check/change the entity before remove.
     */
    protected void preRemove(T entity) {
    }

    /**
     * Deletes an entity on the database without performing the normal user level access check. Should only be used by
     * sublcasses in special cases where you know exactly what you are doing.
     *
     * @param entity The entity to remove
     */
    protected void removeWithoutAccessCheck(T entity) {
        preRemove(entity);
        entityManager.remove(entity);
    }

    /**
     * Finds an entity by its ID.
     *
     * @param id The ID of the entity to look for
     * @return The entity from the database or null if it was not found
     */
    public T find(Number id) {
        T entity = entityManager.find(entityType, id);
        // TODO check access
        return entity;
    }

    /**
     * Returns a type-safe result list which contains all entities of the provided type.
     *
     * @return the type-safe result list.
     */
    public List<T> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityType);
        CriteriaQuery<T> all = query.select(query.from(entityType));
        return resultList(entityManager.createQuery(all));
    }

    /**
     * Returns a type-safe result list of the given query.
     *
     * @param query the query
     * @return the result list
     */
    protected List<T> resultList(TypedQuery<T> query) {
        List<T> entityList = query.getResultList();
        // TODO check access
        return entityList;
    }

    /**
     * Returns a type-safe single result of the given query or null.
     *
     * @param query Typed query to get result from
     * @return the result or null
     * @throws NonUniqueResultException if more than one result
     */
    protected T singleResult(TypedQuery<T> query) {
        List<T> resultList = resultList(query);

        if (resultList.isEmpty()) {
            return null;
        }

        if (resultList.size() > 1) {
            // maybe the result is a join, so make it distinct.
            Set<T> distinctResult = new HashSet<>(resultList);
            if (distinctResult.size() > 1) {
                throw new NonUniqueResultException("Result for query '" + query + "' must contain exactly one item");
            }
        }

        return resultList.get(0);
    }

    protected Long countResult(TypedQuery<Long> query) {
        return query.getSingleResult();
    }

    protected TypedQuery<T> createNamedQuery(String queryName) {
        return entityManager.createNamedQuery(queryName, entityType);
    }

    protected TypedQuery<T> createQuery(String query) {
        return entityManager.createQuery(query, entityType);
    }

    protected TypedQuery<Long> createNamedCountQuery(String query) {
        return entityManager.createNamedQuery(query, Long.class);
    }

}