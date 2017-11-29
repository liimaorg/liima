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

    public List<AuditViewEntry> getAllRevisionsForPropertyEntity(ResourceEditProperty resourceEditProperty) {
        Integer propertyId = resourceEditProperty.getPropertyId();
        List<AuditViewEntry> auditViewEntries = new ArrayList<>();
        if (propertyId != null) {
            PropertyEntity entity = entityManager.find(PropertyEntity.class, propertyId);
            List<Object[]> allRevisionsForEntity = getAllRevisionsForEntity(entity, resourceEditProperty.getPropertyId());

            for (Object o : allRevisionsForEntity) {
                Object[] objects = (Object[]) o;
                PropertyEntity entityForRevision = (PropertyEntity) objects[0];
                MyRevisionEntity revisionEntity = (MyRevisionEntity) objects[1];
                RevisionType revisionType = (RevisionType) objects[2];

                AuditViewEntry auditViewEntry = AuditViewEntry
                        .builder(revisionEntity, revisionType)
                        .value(entityForRevision.getValue())
                        .type("Property")
                        .name(entityForRevision.getDescriptor().getPropertyName())
                        .build();
                auditViewEntries.add(auditViewEntry);
            }
        }
        return auditViewEntries;
    }

    public List<AuditViewEntry> getAuditViewEntriesForResource(int resourceId) {
        List<AuditViewEntry> allAuditViewEntries = new ArrayList<>();
        // load all auditViewEntries for properties of resource
        // load all auditViewEntries for propertyDescriptors of resources properties
        // load all auditViewEntries for the relations of the resource
        // load all auditViewEntries for the templates of the resource
        return allAuditViewEntries;
    }

    public List<AuditViewEntry> getAuditViewEntriesForResourceType(int resourceTypeId) {

        List<AuditViewEntry> allAuditViewEntries = new ArrayList<>();
        // load all auditViewEntries for properties of resourceType
        // load all auditViewEntries for propertyDescriptors of resourceTypes properties
        // load all auditViewEntries for the relations of the resourceType
        // load all auditViewEntries for the templates of the resourceType
        return allAuditViewEntries;
    }

    public List<AuditViewEntry> getAuditViewEntriesForEditProperties(List<ResourceEditProperty> propertiesForResource) {
        List<AuditViewEntry> allAuditViewEntries = new ArrayList<>();

//        List<ResourceEditProperty> properties = new ArrayList<>();
//        List<ResourceEditProperty> propertyDescriptors = new ArrayList<>();
        for (ResourceEditProperty resourceEditProperty : propertiesForResource) {
            allAuditViewEntries.addAll(getAllRevisionsForPropertyEntity(resourceEditProperty));
        }
        return allAuditViewEntries;
    }
}
