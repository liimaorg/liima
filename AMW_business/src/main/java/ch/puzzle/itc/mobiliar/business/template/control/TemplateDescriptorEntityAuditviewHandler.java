package ch.puzzle.itc.mobiliar.business.template.control;

import ch.puzzle.itc.mobiliar.business.auditview.control.GenericAuditHandler;
import ch.puzzle.itc.mobiliar.business.auditview.control.TemplateNotOnConsumedResourceException;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntryContainer;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.NoResultException;

import static ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry.RELATION_CONSUMED_RESOURCE;

@Stateless
@Named("templateDescriptorEntityAuditviewHandler")
public class TemplateDescriptorEntityAuditviewHandler extends GenericAuditHandler {


    private static final String SELECT_IDENTIFIER_FOR_CONSUMED_RESOURCE =
            " SELECT consumed_resource.NAME ||" +
                    "   CASE WHEN consumed_resource_relation.IDENTIFIER IS NOT NULL " +
                    "     THEN ' (' || consumed_resource_relation.IDENTIFIER || ')' " +
                    "     ELSE '' " +
                    "   END" +
                    " FROM TAMW_RESRELCTX_TMPLDESC resrelcontexttemplate" +
                    " JOIN TAMW_RESRELCONTEXT resource_relation_context ON resrelcontexttemplate.TAMW_RESRELCONTEXT_ID = resource_relation_context.ID" +
                    " JOIN TAMW_CONSUMEDRESREL consumed_resource_relation ON resource_relation_context.CONSUMEDRESOURCERELATION_ID = consumed_resource_relation.ID" +
                    " JOIN TAMW_RESOURCE consumed_resource ON consumed_resource.ID = consumed_resource_relation.SLAVERESOURCE_ID" +
                    " WHERE resrelcontexttemplate.TEMPLATES_ID = :templateDescriptorId";

    private static final String SELECT_IDENTIFIER_FOR_CONSUMED_RESOURCE_IN_AUDIT =
            "SELECT DISTINCT consumed_resource.NAME ||" +
                    "  CASE WHEN consumed_resource_relation.IDENTIFIER IS NOT NULL " +
                    "    THEN ' (' || consumed_resource_relation.IDENTIFIER || ')' " +
                    "    ELSE ''" +
                    "  END name" +
                    " FROM TAMW_RESRELCTX_TMPLDESC_AUD resrelcontexttemplate" +
                    " JOIN TAMW_RESRELCONTEXT_AUD resource_relation_context ON resrelcontexttemplate.TAMW_RESRELCONTEXT_ID = resource_relation_context.ID" +
                    " JOIN TAMW_CONSUMEDRESREL_AUD consumed_resource_relation ON resource_relation_context.CONSUMEDRESOURCERELATION_ID = consumed_resource_relation.ID" +
                    " JOIN TAMW_RESOURCE_AUD consumed_resource ON consumed_resource.ID = consumed_resource_relation.SLAVERESOURCE_ID" +
                    " JOIN TAMW_TEMPLATEDESCRIPTOR_AUD templateDescriptor ON resrelcontexttemplate.TEMPLATES_ID = templateDescriptor.ID" +
                    " WHERE resrelcontexttemplate.TEMPLATES_ID = :templateDescriptorId " +
                    " AND templateDescriptor.REV >= :revision " +
                    " AND rownum <= 1";


    @Override
    public AuditViewEntry createAuditViewEntry(AuditViewEntryContainer container) {
        try {
            container.setRelationName(String.format("%s: %s",
                    RELATION_CONSUMED_RESOURCE,
                    getRelationNameOfConsumedResource(container)
            ));
        } catch (TemplateNotOnConsumedResourceException e) {
            // do nothing
        }
        return super.createAuditViewEntry(container);
    }


    /**
     * @param container
     * @return a Tuple&lt;Name of consumed Resource, ContextId&gt; or null
     */
    private String getRelationNameOfConsumedResource(AuditViewEntryContainer container) throws TemplateNotOnConsumedResourceException {
        try {
            return (String) entityManager
                    .createNativeQuery(SELECT_IDENTIFIER_FOR_CONSUMED_RESOURCE)
                    .setParameter("templateDescriptorId", container.getEntityForRevision().getId())
                    .getSingleResult();
        } catch (NoResultException e) {
            try {
                return (String) entityManager
                        .createNativeQuery(SELECT_IDENTIFIER_FOR_CONSUMED_RESOURCE_IN_AUDIT)
                        .setParameter("templateDescriptorId", container.getEntityForRevision().getId())
                        .setParameter("revision", container.getRevEntity().getId())
                        .getSingleResult();
            } catch (NoResultException ef) {
                throw new TemplateNotOnConsumedResourceException();
            }
        }
    }

}
