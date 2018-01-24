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
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntryContainer;
import ch.puzzle.itc.mobiliar.business.auditview.entity.Auditable;
import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.HasContexts;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyEntityAuditviewHandler;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.CrossTypeRevisionChangesReader;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static org.hibernate.envers.RevisionType.ADD;
import static org.hibernate.envers.RevisionType.DEL;

@Stateless
public class AuditService {

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    @Named("genericAuditHandler")
    GenericAuditHandler genericAuditHandler;

    @Inject
    @Named("propertyEntityAuditviewHandler")
    PropertyEntityAuditviewHandler propertyEntityAuditviewHandler;

    Map<Class<? extends Auditable>, GenericAuditHandler> auditHandlerRegistry;

    @PostConstruct
    public void init() {
        auditHandlerRegistry = new HashMap<>();
        auditHandlerRegistry.put(PropertyEntity.class, propertyEntityAuditviewHandler);
        auditHandlerRegistry.put(PropertyDescriptorEntity.class, genericAuditHandler);
    }

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
                createAuditViewEntriesAndAddToMap(allAuditViewEntries, resultList);
            }
        }
        return new ArrayList<>(allAuditViewEntries.values());
    }

    private void createAuditViewEntriesAndAddToMap(Map<Integer, AuditViewEntry> allAuditViewEntries, List resultList) {
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
        AuditViewEntryContainer auditViewEntryContainer = new AuditViewEntryContainer(entityRevisionAndRevisionType);

        GenericAuditHandler handler = auditHandlerRegistry.get(entity.getClass());
        if (handler != null) {
            AuditViewEntry auditViewEntry = handler.createAuditViewEntry(auditViewEntryContainer);
            if (isAuditViewEntryRelevant(auditViewEntry, allAuditViewEntries)) {
                allAuditViewEntries.put(auditViewEntry.hashCode(), auditViewEntry);
            }
        }

    }

    protected boolean isAuditViewEntryRelevant(AuditViewEntry entry, Map<Integer, AuditViewEntry> allAuditViewEntries) {
        if (entry == null) {
            return false;
        }
        if (allAuditViewEntries.get(entry.hashCode()) != null) {
            return false;
        }
        if (entry.getMode() == ADD) {
            return true;
        }
        return !StringUtils.equals(entry.getOldValue(), entry.getValue());
    }

    private List<MyRevisionEntity> getRevisionsForResource(Integer resourceId) {
        return this.entityManager
                    .createQuery("FROM MyRevisionEntity n WHERE n.resourceId = :resourceId", MyRevisionEntity.class)
                    .setParameter("resourceId", resourceId)
                    .getResultList();
    }

    public void storeIdInThreadLocalForAuditLog(HasContexts<?> hasContexts) {
        if (hasContexts instanceof ResourceTypeEntity) {
            setResourceTypeIdInThreadLocal(hasContexts.getId());
        } else if (hasContexts instanceof ResourceEntity) {
            setResourceIdInThreadLocal(hasContexts.getId());
        } else if (hasContexts instanceof ResourceRelationTypeEntity) {
            ThreadLocalUtil.setThreadVariable("ThreadLocalUtil.KEY_RESOURCE_RELATION_TYPE_ID", hasContexts.getId());
        }
    }

    public void setResourceTypeIdInThreadLocal(int resourceTypeId) {
        ThreadLocalUtil.setThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID, resourceTypeId);
    }

    public void setResourceIdInThreadLocal(int resourceId) {
        ThreadLocalUtil.setThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID, resourceId);
    }

    /**
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

}
