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

import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry;
import ch.puzzle.itc.mobiliar.business.auditview.entity.Auditable;
import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextRepository;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.HasContexts;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.CrossTypeRevisionChangesReader;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;

import static org.hibernate.envers.RevisionType.DEL;

public class AuditService {

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    ContextRepository contextRepository;

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
        // Map<Hashcode, AuditViewEntry>
        Map<Integer, AuditViewEntry> allAuditViewEntries = new HashMap<>();
        AuditReader reader = AuditReaderFactory.get(entityManager);
        CrossTypeRevisionChangesReader crossTypeRevisionChangesReader = reader.getCrossTypeRevisionChangesReader();
        List<MyRevisionEntity> revisionsForResource = getRevisionsForResource(resourceId);
        for (MyRevisionEntity revisionEntity : revisionsForResource) {
            List<Object> changedEntitiesForRevision = crossTypeRevisionChangesReader.findEntities(revisionEntity.getId());
            for (Object o : changedEntitiesForRevision) {
                List resultList = reader.createQuery()
                        .forRevisionsOfEntity(o.getClass(), false, true)
                        .add(AuditEntity.revisionNumber().eq(revisionEntity.getId()))
                        .getResultList();
                if (resultList.size() == 1) {
                    createSingleAuditViewEntryAndAddToMap(allAuditViewEntries, (Object[]) resultList.get(0));
                    continue;
                } else {
                    createMultipleAuditViewEntriesAndAddToMap(allAuditViewEntries, resultList);
                    continue;
                }
            }
        }
        return new ArrayList<>(allAuditViewEntries.values());
    }

    private void createMultipleAuditViewEntriesAndAddToMap(Map<Integer, AuditViewEntry> allAuditViewEntries, List resultList) {
        for (Object triple : resultList) {
            createSingleAuditViewEntryAndAddToMap(allAuditViewEntries, (Object[]) triple);
        }
    }

    private void createSingleAuditViewEntryAndAddToMap(Map<Integer, AuditViewEntry> allAuditViewEntries, Object[] entityRevisionAndRevisionType) {
        Object entity = entityRevisionAndRevisionType[0];
        if (! (entity instanceof Auditable) ) {
            System.out.println("NOT IMPLEMENTED YET FOR ENTITY: " + entity.getClass());
            return;
        }

        AuditViewEntry auditViewEntry;
        if (entity instanceof PropertyEntity) {
            auditViewEntry = createAuditViewEntryForProperty(entityRevisionAndRevisionType);
        } else {
            auditViewEntry = createAuditViewEntry(entityRevisionAndRevisionType);
        }
        if (isAuditViewEntryRelevant(auditViewEntry, allAuditViewEntries)) {
            allAuditViewEntries.put(auditViewEntry.hashCode(), auditViewEntry);
        }
    }

    private AuditViewEntry createAuditViewEntryForProperty(Object[] tripleForRevision) {
        Auditable entityForRevision = (Auditable) tripleForRevision[0];
        MyRevisionEntity revEntity = (MyRevisionEntity) tripleForRevision[1];
        RevisionType revisionType = (RevisionType) tripleForRevision[2];

        int contextId = getContextIdForProperty(tripleForRevision);
        revEntity.setEditContextId(contextId);
        return createAuditViewEntry(entityForRevision, revEntity, revisionType);
    }

    private int getContextIdForProperty(Object[] tripleForRevision) {
        PropertyEntity entityForRevision = (PropertyEntity) tripleForRevision[0];
        MyRevisionEntity revEntity = (MyRevisionEntity) tripleForRevision[1];

        BigDecimal resourceContextId;
        try {
            String select = "SELECT TAMW_RESOURCECONTEXT_ID " +
                    "FROM TAMW_RESOURCECTX_PROP " +
                    "WHERE PROPERTIES_ID = :propertyId";
            Query query = entityManager
                    .createNativeQuery(select)
                    .setParameter("propertyId", entityForRevision.getId());
            resourceContextId = (BigDecimal) query.getSingleResult();
        } catch (NoResultException e) {
            String selectFromAudit = "SELECT TAMW_RESOURCECONTEXT_ID " +
                    "FROM TAMW_RESOURCECTX_PROP_AUD " +
                    "WHERE rev >= :rev " +
                    "AND PROPERTIES_ID = :propertyId";
            Query query = entityManager
                    .createNativeQuery(selectFromAudit)
                    .setParameter("rev", revEntity.getId())
                    .setParameter("propertyId", entityForRevision.getId());
            resourceContextId = (BigDecimal) query.getSingleResult();
        }

        AuditReader reader = AuditReaderFactory.get(entityManager);
        ResourceContextEntity resourceContextEntity = (ResourceContextEntity) reader.createQuery()
                .forRevisionsOfEntity(ResourceContextEntity.class, true, true)
                .add(AuditEntity.id().eq(resourceContextId.intValue()))
                .setMaxResults(1)
                .getSingleResult();
        return resourceContextEntity.getId();
    }

    protected boolean isAuditViewEntryRelevant(AuditViewEntry entry, Map<Integer, AuditViewEntry> allAuditViewEntries) {
        if (entry == null) {
            return false;
        }
        if (allAuditViewEntries.get(entry.hashCode()) != null) {
            return false;
        }
        return !StringUtils.equals(entry.getOldValue(), entry.getValue());
    }

    private AuditViewEntry createAuditViewEntry(Auditable entityForRevision, MyRevisionEntity revEntity, RevisionType revisionType) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        Auditable previous = getPrevious(reader, entityForRevision, revEntity);
        String newValueForAuditLog = revisionType == DEL ? StringUtils.EMPTY : entityForRevision.getNewValueForAuditLog();
        return AuditViewEntry
                .builder(revEntity, revisionType)
                .oldValue(previous == null ? StringUtils.EMPTY : previous.getNewValueForAuditLog())
                .value(newValueForAuditLog)
                .type(entityForRevision.getType())
                .name(entityForRevision.getNameForAuditLog())
                .editContextName(getContextName(revEntity.getEditContextId()))
                .build();
    }

    private AuditViewEntry createAuditViewEntry(Object[] entityRevisionAndRevisionType) {
        Auditable entityForRevision = (Auditable) entityRevisionAndRevisionType[0];
        MyRevisionEntity revEntity = (MyRevisionEntity) entityRevisionAndRevisionType[1];
        RevisionType revisionType = (RevisionType) entityRevisionAndRevisionType[2];
        return createAuditViewEntry(entityForRevision, revEntity, revisionType);
    }

    private String getContextName(Integer editContextId) {
        if (editContextId == null) {
            return StringUtils.EMPTY;
        }
        ContextEntity contextEntity = contextRepository.find(editContextId);
        if (contextEntity == null) {
            return String.format("Context with id %i does not exist.", editContextId);
        }
        return contextEntity.getName();
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
            Object[] enversTriple = (Object[]) o;
            AuditViewEntry auditViewEntry = createAuditViewEntry(enversTriple);
            auditViewEntries.add(auditViewEntry);
        }
        return auditViewEntries;
    }

}
