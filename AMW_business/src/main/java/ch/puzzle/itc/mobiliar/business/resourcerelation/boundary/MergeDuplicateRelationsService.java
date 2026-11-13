package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.resourceactivation.entity.ResourceActivationEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import lombok.Data;
import org.hibernate.envers.AuditReaderFactory;

import javax.ejb.ApplicationException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class MergeDuplicateRelationsService {
    @Inject
    EntityManager entityManager;
    @Inject
    PermissionBoundary permissionBoundary;
    @Data
    public static class MigrationResult {
        private boolean applied;
        private Integer auditRevisionId;
        private List<Result> groups = new ArrayList<>();

        public int getGroupCount() {
            return groups.size();
        }

        public int getSurplusRelationCount() {
            return groups.stream().mapToInt(group -> group.getRelationIds().size() - 1).sum();
        }
    }

    @Data
    public static class Result {
        private String mode;
        private Integer masterId;
        private Integer slaveId;
        private String identifier;
        private Integer retainedId;
        private List<Integer> relationIds;
        private List<String> discarded = new ArrayList<>();
        private boolean applied;
    }

    @ApplicationException(rollback = true)
    public static class MergeException extends RuntimeException {
        public MergeException(String message) {
            super(message);
        }
    }

    public MigrationResult migrate(boolean dryRun) {
        MigrationResult result = new MigrationResult();
        result.setGroups(discoverGroups());
        for (Result group : result.getGroups()) {
            try {
                mergeGroup(group, dryRun);
            } catch (MergeException e) {
                throw new MergeException("Cannot merge " + group.getMode() + " relations for master="
                        + group.getMasterId() + " slave=" + group.getSlaveId() + ": " + e.getMessage());
            }
        }
        if (!dryRun && !result.getGroups().isEmpty()) {
            entityManager.flush();
            if (!discoverGroups().isEmpty()) {
                throw new MergeException("Duplicate inventory changed during migration; pause all writers and retry");
            }
            // A transaction has one Envers revision. A multi-resource migration
            // must not attribute every change to whichever master was processed last.
            MyRevisionEntity revision = AuditReaderFactory.get(entityManager).getCurrentRevision(MyRevisionEntity.class, true);
            Integer masterId = result.getGroups().get(0).getMasterId();
            revision.setResourceId(result.getGroups().stream().allMatch(g -> g.getMasterId().equals(masterId))
                    ? masterId : null);
            revision.setResourceTypeId(null);
            result.setAuditRevisionId(revision.getId());
        }
        result.setApplied(!dryRun);
        return result;
    }

    private List<Result> discoverGroups() {
        List<Result> groups = new ArrayList<>();
        for (String mode : Arrays.asList("consumed", "provided")) {
            String entity = "consumed".equals(mode) ? "ConsumedResourceRelationEntity" : "ProvidedResourceRelationEntity";
            List<Object[]> rows = entityManager.createQuery(
                    "select r.masterResource.id, r.slaveResource.id, r.identifier from " + entity + " r "
                            + "where r.masterResource is not null and r.slaveResource is not null "
                            + "group by r.masterResource.id, r.slaveResource.id, r.identifier having count(r) > 1",
                    Object[].class).getResultList();
            for (Object[] row : rows) {
                Result group = new Result();
                group.setMode(mode);
                group.setMasterId((Integer) row[0]);
                group.setSlaveId((Integer) row[1]);
                group.setIdentifier((String) row[2]);
                groups.add(group);
            }
        }
        groups.sort(Comparator.comparing(Result::getMasterId)
                .thenComparing(Result::getSlaveId)
                .thenComparing(Result::getMode)
                .thenComparing(Result::getIdentifier, Comparator.nullsFirst(Comparator.naturalOrder())));
        return groups;
    }

    private void mergeGroup(Result result, boolean dryRun) {
        Integer masterId = result.getMasterId();
        ResourceEntity master = entityManager.find(ResourceEntity.class, masterId, LockModeType.PESSIMISTIC_WRITE);
        if (master == null) {
            throw new MergeException("Master resource not found");
        }
        requirePermission(master, entityManager.find(ContextEntity.class, 1));
        String entity = "consumed".equals(result.getMode())
                ? "ConsumedResourceRelationEntity" : "ProvidedResourceRelationEntity";
        TypedQuery<AbstractResourceRelationEntity> query = entityManager.createQuery(
                "select r from " + entity + " r where r.masterResource.id = :master "
                        + "and r.slaveResource.id = :slave and "
                        + (result.getIdentifier() == null ? "r.identifier is null" : "r.identifier = :identifier")
                        + " order by r.id", AbstractResourceRelationEntity.class)
                .setParameter("master", masterId).setParameter("slave", result.getSlaveId());
        if (result.getIdentifier() != null) {
            query.setParameter("identifier", result.getIdentifier());
        }
        List<AbstractResourceRelationEntity> relations = query.setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
        if (relations.size() < 2) {
            throw new MergeException("Duplicate inventory changed during migration; pause all writers and retry");
        }
        result.setRetainedId(relations.get(0).getId());
        result.setRelationIds(relations.stream().map(AbstractResourceRelationEntity::getId).collect(Collectors.toList()));
        List<ResourceRelationContextEntity> originals = new ArrayList<>();
        Set<Integer> propertyIds = new HashSet<>();
        Map<Integer, PropertyDescriptorEntity> localDescriptors = new LinkedHashMap<>();
        for (AbstractResourceRelationEntity relation : relations) {
            List<ResourceRelationContextEntity> contexts = new ArrayList<>(relation.getNullSaveContexts());
            contexts.sort(Comparator.comparing(c -> {
                if (c.getContext() == null) {
                    throw new MergeException("Relation context has no environment");
                }
                return c.getContext().getId();
            }));
            Set<Integer> environments = new HashSet<>();
            for (ResourceRelationContextEntity context : contexts) {
                if (context.getContext() == null || !environments.add(context.getContext().getId())
                        || (context.getConsumedResourceRelation() != null
                            && context.getProvidedResourceRelation() != null)) {
                    throw new MergeException("Invalid or repeated environment context");
                }
                requirePermission(master, context.getContext());
                originals.add(context);
                for (PropertyDescriptorEntity descriptor : sorted(context.getPropertyDescriptors())) {
                    localDescriptors.putIfAbsent(descriptor.getId(), descriptor);
                }
                for (PropertyEntity property : sorted(context.getProperties())) {
                    propertyIds.add(property.getId());
                }
            }
        }
        // Deleting a relation-local descriptor cascades to its values, including
        // values outside this group if the database contains shared ownership.
        for (PropertyDescriptorEntity descriptor : localDescriptors.values()) {
            if (descriptor.getProperties().stream().anyMatch(p -> !propertyIds.contains(p.getId()))) {
                throw new MergeException("Local descriptor " + descriptor.getId()
                        + " is used outside this duplicate group; refusing cascading data loss");
            }
        }
        AbstractResourceRelationEntity keeper = relations.get(0);
        Map<Integer, ResourceRelationContextEntity> merged = new LinkedHashMap<>();
        Map<String, PropertyDescriptorEntity> descriptorsByName = new LinkedHashMap<>();
        for (ResourceRelationContextEntity original : originals) {
            ResourceRelationContextEntity target = targetContext(merged, keeper, original.getContext());
            for (PropertyDescriptorEntity descriptor : sorted(original.getPropertyDescriptors())) {
                String name = descriptor.getPropertyName();
                if (descriptorsByName.containsKey(name)) {
                    discard(result, original, "descriptor", name);
                } else {
                    PropertyDescriptorEntity copy = descriptor.getCopy(null, null);
                    copy.setPropertyTags(new ArrayList<>(descriptor.getPropertyTags()));
                    descriptorsByName.put(name, copy);
                    target.addPropertyDescriptor(copy);
                }
            }
        }
        for (ResourceRelationContextEntity original : originals) {
            ResourceRelationContextEntity target = merged.get(original.getContext().getId());
            for (PropertyEntity property : sorted(original.getProperties())) {
                String name = property.getDescriptor().getPropertyName();
                if (sorted(target.getProperties()).stream().anyMatch(p -> Objects.equals(p.getDescriptor().getPropertyName(), name))) {
                    discard(result, original, "property", name);
                    continue;
                }
                PropertyEntity copy = property.copy();
                PropertyDescriptorEntity descriptor = descriptorsByName.getOrDefault(name, property.getDescriptor());
                copy.setDescriptor(descriptor);
                if (descriptor.isEncrypt() != property.getDescriptor().isEncrypt()) {
                    result.getDiscarded().add("encryption conversion key=" + name
                            + " relation=" + original.getContextualizedObject().getId()
                            + " context=" + original.getContext().getId()
                            + " fromEncrypted=" + property.getDescriptor().isEncrypt()
                            + " toEncrypted=" + descriptor.isEncrypt());
                    copy.setValueAndEncrypt(property.getDecryptedValue());
                }
                target.addProperty(copy);
                if (descriptorsByName.containsKey(name)) {
                    descriptor.addProperty(copy);
                }
            }
            for (TemplateDescriptorEntity template : sorted(original.getTemplates())) {
                if (sorted(target.getTemplates()).stream().anyMatch(t -> Objects.equals(t.getName(), template.getName()))) {
                    discard(result, original, "template", template.getName());
                } else {
                    TemplateDescriptorEntity copy = template.getCopy(null, null);
                    target.addTemplate(copy);
                }
            }
            for (ResourceActivationEntity activation : sortedActivations(original)) {
                Integer group = activation.getResourceGroup().getId();
                if (target.getResourceActivationEntities().stream().anyMatch(a -> a.getResourceGroup().getId().equals(group))) {
                    discard(result, original, "activation", group.toString());
                } else {
                    ResourceActivationEntity copy = new ResourceActivationEntity();
                    copy.setActive(activation.isActive());
                    copy.setResourceGroup(activation.getResourceGroup());
                    copy.setResourceRelationContext(target);
                    target.getResourceActivationEntities().add(copy);
                }
            }
        }
        for (AbstractResourceRelationEntity relation : relations.subList(1, relations.size())) {
            if (!Objects.equals(typeId(keeper), typeId(relation))) {
                result.getDiscarded().add("relationType relation=" + relation.getId()
                        + " retainedRelation=" + keeper.getId());
            }
        }
        if (dryRun) {
            return;
        }
        for (AbstractResourceRelationEntity relation : relations) {
            relation.setContexts(new HashSet<>());
        }
        for (ResourceRelationContextEntity original : originals) {
            entityManager.remove(original);
        }
        for (PropertyDescriptorEntity descriptor : descriptorsByName.values()) {
            entityManager.persist(descriptor);
        }
        for (ResourceRelationContextEntity context : merged.values()) {
            keeper.addContext(context);
            entityManager.persist(context);
            for (ResourceActivationEntity activation : context.getResourceActivationEntities()) {
                entityManager.persist(activation);
            }
        }
        for (AbstractResourceRelationEntity relation : relations.subList(1, relations.size())) {
            master.removeRelation(relation);
            relation.getSlaveResource().removeSlaveRelation(relation);
            entityManager.remove(relation);
        }
        result.setApplied(true);
    }

    private Integer typeId(AbstractResourceRelationEntity relation) {
        return relation.getResourceRelationType() == null ? null : relation.getResourceRelationType().getId();
    }

    private void requirePermission(ResourceEntity master, ContextEntity context) {
        if (context == null || !permissionBoundary.hasPermission(Permission.RESOURCE, context, Action.UPDATE, master, null)) {
            throw new NotAuthorizedException("Not allowed to merge relations in every affected context");
        }
    }

    private ResourceRelationContextEntity targetContext(Map<Integer, ResourceRelationContextEntity> contexts,
                                                        AbstractResourceRelationEntity keeper, ContextEntity environment) {
        return contexts.computeIfAbsent(environment.getId(), id -> {
            ResourceRelationContextEntity context = new ResourceRelationContextEntity();
            context.setContext(environment);
            context.setContextualizedObject(keeper);
            context.setPropertyDescriptors(new HashSet<>());
            context.setResourceActivationEntities(new HashSet<>());
            return context;
        });
    }

    private void discard(Result result, ResourceRelationContextEntity context, String kind, String key) {
        result.getDiscarded().add(kind + " key=" + key + " relation=" + context.getContextualizedObject().getId()
                + " context=" + context.getContext().getId() + " (earlier definition retained)");
    }

    private <T extends Identifiable> List<T> sorted(Set<T> values) {
        List<T> result = values == null ? new ArrayList<>() : new ArrayList<>(values);
        result.sort(Comparator.comparing(Identifiable::getId,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    private List<ResourceActivationEntity> sortedActivations(ResourceRelationContextEntity context) {
        List<ResourceActivationEntity> result = context.getResourceActivationEntities() == null
                ? new ArrayList<>() : new ArrayList<>(context.getResourceActivationEntities());
        result.sort(Comparator.comparing(ResourceActivationEntity::getId));
        return result;
    }

}
