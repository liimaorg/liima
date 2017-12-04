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

import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hibernate.envers.RevisionType.DEL;

public class AuditService {

    @PersistenceContext
    EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public <T> Object getDeletedEntity(T entity, Integer id) {
        Objects.requireNonNull(entity, "Entity can not be null");
        Objects.requireNonNull(id, "Id can not be null");

        AuditReader reader = AuditReaderFactory.get(entityManager);

        if (reader.isEntityClassAudited(entity.getClass())) {
            AuditQuery query = reader.createQuery()
                    .forRevisionsOfEntity(entity.getClass(), false, true)
                    .add(AuditEntity.id().eq(id))
                    .add(AuditEntity.revisionType().eq(DEL));

            List<Object[]> resultList = query.getResultList();
            if (!resultList.isEmpty()) {
                return resultList.get(0)[0];
            }
        }
        return null;
    }

//    public List<AuditViewEntry> getAuditViewEntriesForResource(int id) {
//        ResourceEntity entity = entityManager.find(ResourceEntity.class, id);
//        List allRevisionsForEntity = getAllRevisionsForEntity(entity, id);
//        List auditViewEntries = createAuditViewEntries(allRevisionsForEntity);
//        return allRevisionsForEntity;
//    }

    public List<AuditViewEntry> getAuditViewEntriesForProperties(List<ResourceEditProperty> propertiesForResource) {
        List<AuditViewEntry> allAuditViewEntries = new ArrayList<>();
        for (ResourceEditProperty resourceEditProperty : propertiesForResource) {
            allAuditViewEntries.addAll(getAllRevisionsForPropertyEntity(resourceEditProperty));
        }
        return allAuditViewEntries;
    }

    public List<AuditViewEntry> getAuditViewEntriesForPropertyDescriptors(List<ResourceEditProperty> propertiesForResource) {
        List<AuditViewEntry> allAuditViewEntries = new ArrayList<>();
        for (ResourceEditProperty resourceEditProperty : propertiesForResource) {
            allAuditViewEntries.addAll(getAllRevisionsForPropertyDescriptorEntity(resourceEditProperty));
        }
        return allAuditViewEntries;
    }

    /**
     * @param entity
     * @param id
     * @param <T>
     * @return a list of three-element arrays, containing:
     * <ol>
     * <li>the entity instance</li>
     * <li>revision entity, corresponding to the revision at which the entity was modified. If no custom
     * revision entity is used, this will be an instance of {@link org.hibernate.envers.DefaultRevisionEntity}</li>
     * <li>type of the revision (an enum instance of class {@link org.hibernate.envers.RevisionType})</li>
     * </ol>
     */
    private <T> List getAllRevisionsForEntity(T entity, Integer id){
        Objects.requireNonNull(entity, "Entity can not be null");
        Objects.requireNonNull(id, "Id can not be null");

        AuditReader reader = AuditReaderFactory.get(entityManager);

        if (reader.isEntityClassAudited(entity.getClass())) {
            AuditQuery query = reader.createQuery().forRevisionsOfEntity(entity.getClass(), false, true)
                    .add(AuditEntity.id().eq(id))
                    .addOrder(AuditEntity.revisionNumber().desc());
            List<T> resultList = query.getResultList();
            return resultList;
        }
        return null;
    }

    private List<AuditViewEntry> getAllRevisionsForPropertyEntity(ResourceEditProperty resourceEditProperty) {
        PropertyEntity entity = entityManager.find(PropertyEntity.class, resourceEditProperty.getPropertyId());
        List<Object[]> allRevisionsForEntity = getAllRevisionsForEntity(entity, resourceEditProperty.getPropertyId());
        return createAuditViewEntries(allRevisionsForEntity);
    }

    private List<AuditViewEntry> getAllRevisionsForPropertyDescriptorEntity(ResourceEditProperty resourceEditProperty) {
        PropertyDescriptorEntity entity = entityManager.find(PropertyDescriptorEntity.class, resourceEditProperty.getDescriptorId());
        List<Object[]> allRevisionsForEntity = getAllRevisionsForEntity(entity, resourceEditProperty.getDescriptorId());
        return createAuditViewEntries(allRevisionsForEntity);
    }

    private List<AuditViewEntry> createAuditViewEntries(List<Object[]> allRevisionsForEntity) {
        List<AuditViewEntry> auditViewEntries = new ArrayList<>();
        for (Object o : allRevisionsForEntity) {
            Object[] objects = (Object[]) o;
            Auditable entityForRevision = (Auditable) objects[0];
            MyRevisionEntity revisionEntity = (MyRevisionEntity) objects[1];
            RevisionType revisionType = (RevisionType) objects[2];

            AuditViewEntry auditViewEntry = AuditViewEntry
                    .builder(revisionEntity, revisionType)
                    .value(entityForRevision.getNewValueForAuditLog())
                    .type(entityForRevision.getType())
                    .name(entityForRevision.getNameForAuditLog())
                    .build();
            auditViewEntries.add(auditViewEntry);
        }
        return auditViewEntries;
    }

}
