package ch.puzzle.itc.mobiliar.business.property.control;

import ch.puzzle.itc.mobiliar.business.auditview.control.GenericAuditHandler;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntryContainer;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.math.BigDecimal;

@Stateless
@Named("propertyEntityAuditviewHandler")
public class PropertyEntityAuditviewHandler extends GenericAuditHandler {

    @Override
    public AuditViewEntry createAuditViewEntry(AuditViewEntryContainer auditViewEntryContainer) {
        try {
            setContextIdForProperty(auditViewEntryContainer);
        } catch (NoResultException e) {
            setRelationNameAndContextForPropertyOfRelatedResource(auditViewEntryContainer);
        }
        return super.createAuditViewEntry(auditViewEntryContainer);

    }


    private void setContextIdForProperty(AuditViewEntryContainer container) throws NoResultException {
        String selectForResource = "SELECT TAMW_RESOURCECONTEXT_ID " +
                "FROM TAMW_RESOURCECTX_PROP " +
                "WHERE PROPERTIES_ID = :propertyId";
        String selectForResourceFromAudit = "SELECT TAMW_RESOURCECONTEXT_ID " +
                "FROM TAMW_RESOURCECTX_PROP_AUD " +
                "WHERE rev >= :rev " +
                "AND PROPERTIES_ID = :propertyId";
        String selectForPropertyOnResource = String.format("%s UNION %s", selectForResource, selectForResourceFromAudit);
        Query query = entityManager
                .createNativeQuery(selectForPropertyOnResource)
                .setParameter("rev", container.getRevEntity().getId())
                .setParameter("propertyId", container.getEntityForRevision().getId());
        BigDecimal resourceContextId = (BigDecimal) query.getSingleResult();
        AuditReader reader = AuditReaderFactory.get(entityManager);
        ResourceContextEntity resourceContextEntity = (ResourceContextEntity) reader.createQuery()
                .forRevisionsOfEntity(ResourceContextEntity.class, true, true)
                .add(AuditEntity.id().eq(resourceContextId.intValue()))
                .setMaxResults(1)
                .getSingleResult();
        container.setEditContextId(resourceContextEntity.getId());
    }

    private void setRelationNameAndContextForPropertyOfRelatedResource(AuditViewEntryContainer auditViewEntryContainer) {
        try {
            setRelationNameAndContextForPropertyOfConsumedResource(auditViewEntryContainer);
        } catch (NoResultException e) {
            setRelationNameAndContextForPropertyOfProvidedResource(auditViewEntryContainer);
        }
    }

    private void setRelationNameAndContextForPropertyOfConsumedResource(AuditViewEntryContainer container) {
        String selectNameAndContext =
                " SELECT " +
                        "consumed_resource.NAME || " +
                        "  CASE WHEN consumed_resource_relation.IDENTIFIER IS NOT NULL " +
                        "    THEN ' (' || consumed_resource_relation.IDENTIFIER || ')' " +
                        "    ELSE '' " +
                        "  END, " +
                        "resource_relation_context.CONTEXT_ID " +
                        " FROM TAMW_RESOURCE consumed_resource " +
                        " JOIN TAMW_CONSUMEDRESREL consumed_resource_relation " +
                        "     ON consumed_resource_relation.SLAVERESOURCE_ID = consumed_resource.ID " +
                        " JOIN TAMW_RESRELCONTEXT resource_relation_context " +
                        "     ON consumed_resource_relation.ID = resource_relation_context.CONSUMEDRESOURCERELATION_ID " +
                        " JOIN TAMW_RESRELCTX_PROP resource_relation_context_prop " +
                        "     ON resource_relation_context_prop.TAMW_RESRELCONTEXT_ID = resource_relation_context.ID " +
                        " WHERE resource_relation_context_prop.PROPERTIES_ID = :propertyId";
        executeRelatedResourceQueryAndEnrichAuditViewContainer(container, selectNameAndContext, null, AuditViewEntry.RELATION_CONSUMED_RESOURCE);
    }

    private void setRelationNameAndContextForPropertyOfProvidedResource(AuditViewEntryContainer container) {
        String selectNameAndContext =
                " SELECT provided_resource.NAME, resource_relation_context.CONTEXT_ID " +
                        " FROM TAMW_RESOURCE provided_resource " +
                        " JOIN TAMW_PROVIDEDRESREL provided_resource_relation " +
                        "     ON provided_resource_relation.SLAVERESOURCE_ID = provided_resource.ID " +
                        " JOIN TAMW_RESRELCONTEXT resource_relation_context " +
                        "     ON provided_resource_relation.ID = resource_relation_context.PROVIDEDRESOURCERELATION_ID " +
                        " JOIN TAMW_RESRELCTX_PROP resource_relation_context_prop " +
                        "     ON resource_relation_context_prop.TAMW_RESRELCONTEXT_ID = resource_relation_context.ID " +
                        " WHERE resource_relation_context_prop.PROPERTIES_ID = :propertyId";
//        String selectNameAndContextInAuditLog =
//                " SELECT provided_resource.NAME, resource_relation_context.CONTEXT_ID " +
//                        " FROM TAMW_RESOURCE_AUD provided_resource " +
//                        " JOIN TAMW_PROVIDEDRESREL_AUD provided_resource_relation " +
//                        "     ON provided_resource_relation.SLAVERESOURCE_ID = provided_resource.ID " +
//                        " JOIN TAMW_RESRELCONTEXT_AUD resource_relation_context " +
//                        "     ON provided_resource_relation.ID = resource_relation_context.PROVIDEDRESOURCERELATION_ID " +
//                        " JOIN TAMW_RESRELCTX_PROP_AUD resource_relation_context_prop " +
//                        "     ON resource_relation_context_prop.TAMW_RESRELCONTEXT_ID = resource_relation_context.ID " +
//                        " WHERE resource_relation_context_prop.PROPERTIES_ID = :propertyId " +
//                        " AND ROWNUM = 1" +
//                        " ORDER BY provided_resource_relation.REV DESC";
        executeRelatedResourceQueryAndEnrichAuditViewContainer(container, selectNameAndContext, selectNameAndContext, AuditViewEntry.RELATION_PROVIDED_RESOURCE);
    }

    private void executeRelatedResourceQueryAndEnrichAuditViewContainer(AuditViewEntryContainer container, String select, String selectInAuditLog, String relationName) {
        Object[] nameAndId;
        try {
            nameAndId = (Object[]) entityManager
                    .createNativeQuery(select)
                    .setParameter("propertyId", container.getEntityForRevision().getId())
                    .getSingleResult();
        } catch (NoResultException e) {
            nameAndId = (Object[]) entityManager
                    .createNativeQuery(selectInAuditLog)
                    .setParameter("propertyId", container.getEntityForRevision().getId())
                    .getSingleResult();
        }
        String name = (String) nameAndId[0];
        int resourceContextId = ((BigDecimal) nameAndId[1]).intValue();
        container.setRelationName(String.format("%s: %s", relationName, name));
        container.setEditContextId(resourceContextId);
    }

}
