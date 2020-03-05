package ch.puzzle.itc.mobiliar.business.property.control;

import ch.puzzle.itc.mobiliar.business.auditview.control.AuditHandler;
import ch.puzzle.itc.mobiliar.business.auditview.control.PropertyNotOnConsumedResourceException;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntryContainer;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.common.util.Tuple;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import static ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry.RELATION_CONSUMED_RESOURCE;
import static ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry.RELATION_PROVIDED_RESOURCE;

@Stateless
@Named("propertyEntityAuditviewHandler")
public class PropertyEntityAuditviewHandler extends AuditHandler {
    // Property on Master Resource
    private static final String SELECT_FOR_RESOURCE = "SELECT TAMW_RESOURCECONTEXT_ID as context_id " +
            "FROM TAMW_RESOURCECTX_PROP " +
            "WHERE PROPERTIES_ID = :propertyId";
    private static final String SELECT_FOR_RESOURCE_FROM_AUDIT = "SELECT TAMW_RESOURCECONTEXT_ID as context_id " +
            "FROM TAMW_RESOURCECTX_PROP_AUD " +
            "WHERE rev >= :rev " +
            "AND PROPERTIES_ID = :propertyId";
    private static final String SELECT_FOR_PROP_ON_RESOURCE = String.format("%s UNION %s", SELECT_FOR_RESOURCE, SELECT_FOR_RESOURCE_FROM_AUDIT);

    // Property on Consumed Resource
    private static final String SELECT_NAME_AND_CONTEXT_FOR_PROP_ON_CONSUMED_RESOURCE =
            " SELECT " +
                    " consumed_resource.NAME || " +
                    "  CASE WHEN consumed_resource_relation.IDENTIFIER IS NOT NULL " +
                    "    THEN ' (' || consumed_resource_relation.IDENTIFIER || ')' " +
                    "    ELSE '' " +
                    "  END as resource_name, " +
                    " resource_relation_context.CONTEXT_ID as context_id " +
                    " FROM TAMW_RESOURCE consumed_resource " +
                    " JOIN TAMW_CONSUMEDRESREL consumed_resource_relation " +
                    "     ON consumed_resource_relation.SLAVERESOURCE_ID = consumed_resource.ID " +
                    " JOIN TAMW_RESRELCONTEXT resource_relation_context " +
                    "     ON consumed_resource_relation.ID = resource_relation_context.CONSUMEDRESOURCERELATION_ID " +
                    " JOIN TAMW_RESRELCTX_PROP resource_relation_context_prop " +
                    "     ON resource_relation_context_prop.TAMW_RESRELCONTEXT_ID = resource_relation_context.ID " +
                    " WHERE resource_relation_context_prop.PROPERTIES_ID = :propertyId";

    private static final String SELECT_NAME_AND_CONTEXT_FOR_PROP_ON_CONSUMED_RESOURCE_FROM_AUDIT =
            " SELECT DISTINCT " +
                    "consumed_resource.NAME || " +
                    "  CASE WHEN consumed_resource_relation.IDENTIFIER IS NOT NULL " +
                    "    THEN ' (' || consumed_resource_relation.IDENTIFIER || ')' " +
                    "    ELSE '' " +
                    "  END as resource_name, " +
                    "resource_relation_context.CONTEXT_ID as context_id " +
                    "FROM TAMW_RESOURCE_AUD consumed_resource " +
                    "JOIN TAMW_CONSUMEDRESREL_AUD consumed_resource_relation ON consumed_resource_relation.SLAVERESOURCE_ID = consumed_resource.ID " +
                    "JOIN TAMW_RESRELCONTEXT_AUD resource_relation_context ON consumed_resource_relation.ID = resource_relation_context.CONSUMEDRESOURCERELATION_ID " +
                    "JOIN TAMW_RESRELCTX_PROP_AUD resource_relation_context_prop ON resource_relation_context_prop.TAMW_RESRELCONTEXT_ID = resource_relation_context.ID " +
                    "JOIN TAMW_PROPERTY_AUD property on resource_relation_context_prop.PROPERTIES_ID = property.ID  " +
                    "WHERE  " +
                    "resource_relation_context_prop.PROPERTIES_ID = :propertyId " +
                    "AND property.REV = :revision " +
                    "AND rownum <= 1 " +
                    "ORDER BY property.REV";

    private static final String SELECT_NAME_AND_CONTEXT_FOR_PROP_ON_PROVIDED_RESOURCE =
            " SELECT provided_resource.NAME as resource_name, resource_relation_context.CONTEXT_ID as context_id " +
                    " FROM TAMW_RESOURCE provided_resource " +
                    " JOIN TAMW_PROVIDEDRESREL provided_resource_relation " +
                    "     ON provided_resource_relation.SLAVERESOURCE_ID = provided_resource.ID " +
                    " JOIN TAMW_RESRELCONTEXT resource_relation_context " +
                    "     ON provided_resource_relation.ID = resource_relation_context.PROVIDEDRESOURCERELATION_ID " +
                    " JOIN TAMW_RESRELCTX_PROP resource_relation_context_prop " +
                    "     ON resource_relation_context_prop.TAMW_RESRELCONTEXT_ID = resource_relation_context.ID " +
                    " WHERE resource_relation_context_prop.PROPERTIES_ID = :propertyId";

     private static final String SELECT_NAME_AND_CONTEXT_FOR_PROP_ON_PROVIDED_RESOURCE_FROM_AUDIT =
                " SELECT provided_resource.NAME as resource_name, resource_relation_context.CONTEXT_ID as context_id " +
                        " FROM TAMW_RESOURCE_AUD provided_resource " +
                        " JOIN TAMW_PROVIDEDRESREL_AUD provided_resource_relation " +
                        "     ON provided_resource_relation.SLAVERESOURCE_ID = provided_resource.ID " +
                        " JOIN TAMW_RESRELCONTEXT_AUD resource_relation_context " +
                        "     ON provided_resource_relation.ID = resource_relation_context.PROVIDEDRESOURCERELATION_ID " +
                        " JOIN TAMW_RESRELCTX_PROP_AUD resource_relation_context_prop " +
                        "     ON resource_relation_context_prop.TAMW_RESRELCONTEXT_ID = resource_relation_context.ID " +
                        " JOIN TAMW_PROPERTY_AUD prop " +
                        "     ON prop.ID = resource_relation_context_prop.PROPERTIES_ID " +
                        " WHERE resource_relation_context_prop.PROPERTIES_ID = :propertyId " +
                        " AND ROWNUM = 1" +
                        " ORDER BY provided_resource_relation.REV DESC";


    @Override
    public AuditViewEntry createAuditViewEntry(AuditViewEntryContainer container) {
        ResourceContextEntity resourceContextEntity = getResourceContextEntityForPropertyOnMasterResource(container);
        boolean isPropertyOnMasterResource = resourceContextEntity != null;
        if (isPropertyOnMasterResource) {
            container.setEditContextId(resourceContextEntity.getId());
        } else {
            // property is on related resource (provided/consumed)
            try {
                Tuple<String, Integer> nameAndContext = getNameAndContextOfConsumedResource(container);
                setRelationNameAndEditedContexIdOnContainer(container, nameAndContext, RELATION_CONSUMED_RESOURCE);
            } catch (PropertyNotOnConsumedResourceException e) {
                Tuple<String, Integer> nameAndContext = getNameAndContextOfProvidedResource(container);
                setRelationNameAndEditedContexIdOnContainer(container, nameAndContext, RELATION_PROVIDED_RESOURCE);
            }
        }
        return super.createGenericAuditViewEntry(container);
    }

    private void setRelationNameAndEditedContexIdOnContainer(AuditViewEntryContainer container, Tuple<String, Integer> nameAndContextOfResource, String relationConsumedResource) {
        container.setRelationName(String.format("%s: %s", relationConsumedResource, nameAndContextOfResource.getA()));
        container.setEditContextId(nameAndContextOfResource.getB());
    }

    private Tuple<String, Integer> getNameAndContextOfProvidedResource(AuditViewEntryContainer container) {
        Object[] nameAndId;
        try {
            nameAndId = (Object[]) entityManager
                    .createNativeQuery(SELECT_NAME_AND_CONTEXT_FOR_PROP_ON_PROVIDED_RESOURCE, "NameAndContextResult")
                    .setParameter("propertyId", container.getEntityForRevision().getId())
                    .getSingleResult();
        } catch (NoResultException e) {
            // deleted property, get it from audit tables
            nameAndId = (Object[]) entityManager
                    .createNativeQuery(SELECT_NAME_AND_CONTEXT_FOR_PROP_ON_PROVIDED_RESOURCE_FROM_AUDIT, "NameAndContextResult")
                    .setParameter("propertyId", container.getEntityForRevision().getId())
                    .getSingleResult();
        }
        String name = (String) nameAndId[0];
        int resourceContextId = (int) nameAndId[1];
        return new Tuple<>(name, resourceContextId);
    }

    /**
     *
     * @param container
     * @return a Tuple&lt;Name of consumed Resource, ContextId&gt; or null
     */
    private Tuple<String, Integer> getNameAndContextOfConsumedResource(AuditViewEntryContainer container) throws PropertyNotOnConsumedResourceException {
        Object[] nameAndId;
        try {
            nameAndId = (Object[]) entityManager
                    .createNativeQuery(SELECT_NAME_AND_CONTEXT_FOR_PROP_ON_CONSUMED_RESOURCE, "NameAndContextResult")
                    .setParameter("propertyId", container.getEntityForRevision().getId())
                    .getSingleResult();
        } catch (NoResultException e) {
            try {
                nameAndId = (Object[]) entityManager
                        .createNativeQuery(SELECT_NAME_AND_CONTEXT_FOR_PROP_ON_CONSUMED_RESOURCE_FROM_AUDIT, "NameAndContextResult")
                        .setParameter("propertyId", container.getEntityForRevision().getId())
                        .setParameter("revision", container.getRevEntity().getId())
                        .getSingleResult();
            } catch (NoResultException ef) {
                throw new PropertyNotOnConsumedResourceException();
            }
        }
        String name = (String) nameAndId[0];
        int resourceContextId = (int) nameAndId[1];
        return new Tuple<>(name, resourceContextId);
    }

    public ResourceContextEntity getResourceContextEntityForPropertyOnMasterResource(AuditViewEntryContainer container) {
        try {
            Query query = entityManager
                    .createNativeQuery(SELECT_FOR_PROP_ON_RESOURCE, "ContextResult")
                    .setParameter("rev", container.getRevEntity().getId())
                    .setParameter("propertyId", container.getEntityForRevision().getId());
            int resourceContextId = (int) query.getSingleResult();
            AuditReader reader = AuditReaderFactory.get(entityManager);
            return (ResourceContextEntity) reader.createQuery()
                    .forRevisionsOfEntity(ResourceContextEntity.class, true, true)
                    .add(AuditEntity.id().eq(resourceContextId))
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
