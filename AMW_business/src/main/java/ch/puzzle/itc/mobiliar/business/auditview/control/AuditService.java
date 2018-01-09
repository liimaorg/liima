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

package ch.puzzle.itc.mobiliar.business.auditview.control;

import ch.puzzle.itc.mobiliar.business.auditview.entity.Auditable;
import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.HasContexts;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry;
import ch.puzzle.itc.mobiliar.business.utils.ThreadLocalUtil;
import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.CrossTypeRevisionChangesReader;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.*;

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

    public List<AuditViewEntry> getAuditViewEntriesForResource(Integer resourceId) {
        List<AuditViewEntry> allAuditViewEntries = new ArrayList<>();
        AuditReader reader = AuditReaderFactory.get(entityManager);
        CrossTypeRevisionChangesReader crossTypeRevisionChangesReader = reader.getCrossTypeRevisionChangesReader();
        List<MyRevisionEntity> revisionsForResource = getRevisionsForResource(resourceId);
        for (MyRevisionEntity revisionEntity : revisionsForResource) {
            List<Object> changedEntitiesForRevision = crossTypeRevisionChangesReader.findEntities(revisionEntity.getId());
            for (Object o : changedEntitiesForRevision) {
                Object[] entityRevisionAndRevisionType = (Object[]) reader.createQuery().forRevisionsOfEntity(o.getClass(), false, true)
                        .add(AuditEntity.revisionNumber().eq(revisionEntity.getId()))
                        .setMaxResults(1)
                        .getSingleResult();
                Object entity = entityRevisionAndRevisionType[0];
                if (! (entity instanceof Auditable) ) {
                    System.out.println("NOT IMPLEMENTED YET FOR ENTITY: " + entity.getClass());
                    continue;
                }
                AuditViewEntry auditViewEntry = createAuditViewEntry(entityRevisionAndRevisionType);
                allAuditViewEntries.add(auditViewEntry);
            }
        }
        return allAuditViewEntries;
    }

    private AuditViewEntry createAuditViewEntry(Object[] entityRevisionAndRevisionType) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        Auditable entityForRevision = (Auditable) entityRevisionAndRevisionType[0];
        MyRevisionEntity revEntity = (MyRevisionEntity) entityRevisionAndRevisionType[1];
        RevisionType revisionType = (RevisionType) entityRevisionAndRevisionType[2];
        Auditable previous = getPrevious(reader, entityForRevision, revEntity);
        String newValueForAuditLog = revisionType == DEL ? StringUtils.EMPTY : entityForRevision.getNewValueForAuditLog();
        return AuditViewEntry
                .builder(revEntity, revisionType)
                .oldValue(previous == null ? StringUtils.EMPTY : previous.getNewValueForAuditLog())
                .value(newValueForAuditLog)
                .type(entityForRevision.getType())
                .name(entityForRevision.getNameForAuditLog())
                .build();
    }

    private Auditable getPrevious(AuditReader reader, Auditable entityForRevision, MyRevisionEntity revEntity) {
        try {
            return (Auditable) reader.createQuery().forRevisionsOfEntity(entityForRevision.getClass(), true, true)
                    .add(AuditEntity.id().eq(entityForRevision.getId()))
                    .add(AuditEntity.revisionNumber().lt(revEntity.getId()))
                    .addOrder(AuditEntity.revisionNumber().desc())
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private List<MyRevisionEntity> getRevisionsForResource(Integer resourceId) {
        return this.entityManager
                    .createQuery("FROM MyRevisionEntity n WHERE n.resourceId = :resourceId", MyRevisionEntity.class)
                    .setParameter("resourceId", resourceId)
                    .getResultList();
    }

    public void storeIdInThreadLocalForAuditLog(HasContexts<?> hasContexts, int editingContext) {
        if (hasContexts instanceof ResourceTypeEntity) {
            setResourceTypeIdInThreadLocal(hasContexts.getId(), editingContext);
        } else if (hasContexts instanceof ResourceEntity) {
            setResourceIdInThreadLocal(hasContexts.getId(), editingContext);
        } else if (hasContexts instanceof ResourceRelationTypeEntity) {
            // TODO apollari
            ThreadLocalUtil.setThreadVariable("ThreadLocalUtil.KEY_RESOURCE_RELATION_TYPE_ID", hasContexts.getId());
        }
    }

    public void setResourceTypeIdInThreadLocal(int resourceTypeId, int editingContextId) {
        ThreadLocalUtil.setThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID, resourceTypeId);
        setContextIdInThreadLocal(editingContextId);
    }

    public void setResourceIdInThreadLocal(int resourceId, int editingContextId) {
        ThreadLocalUtil.setThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID, resourceId);
        setContextIdInThreadLocal(editingContextId);
    }

    private void setContextIdInThreadLocal(int editingContext) {
        ThreadLocalUtil.setThreadVariable(ThreadLocalUtil.KEY_EDIT_CONTEXT_ID, editingContext);
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
        Number revisionNumberOneYearAgo = getRevisionNumberOneYearAgo(reader);

        if (reader.isEntityClassAudited(entity.getClass())) {
            AuditQuery query = reader.createQuery().forRevisionsOfEntity(entity.getClass(), false, true)
                    .add(AuditEntity.id().eq(id))
                    .add(AuditEntity.revisionNumber().gt(revisionNumberOneYearAgo))
                    .addOrder(AuditEntity.revisionNumber().desc());
            List<T> resultList = query.getResultList();
            return resultList;
        }
        return null;
    }

    private Number getRevisionNumberOneYearAgo(AuditReader reader) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int currentYear = c.get(Calendar.YEAR);
        c.set(Calendar.YEAR, currentYear -1);
        return reader.getRevisionNumberForDate(c.getTime());
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
            AuditViewEntry auditViewEntry = createAuditViewEntry(objects);
            auditViewEntries.add(auditViewEntry);
        }
        return auditViewEntries;
    }

}
