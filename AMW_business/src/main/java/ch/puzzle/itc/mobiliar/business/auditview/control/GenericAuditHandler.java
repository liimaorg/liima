package ch.puzzle.itc.mobiliar.business.auditview.control;

import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntryContainer;
import ch.puzzle.itc.mobiliar.business.auditview.entity.Auditable;
import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextRepository;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import static org.hibernate.envers.RevisionType.DEL;

@Stateless
@NoArgsConstructor
@Named("genericAuditHandler")
public class GenericAuditHandler {

    @PersistenceContext
    protected EntityManager entityManager;

    @Inject
    protected ContextRepository contextRepository;


    public AuditViewEntry createAuditViewEntry(AuditViewEntryContainer auditViewEntryContainer) {
        Auditable entityForRevision = auditViewEntryContainer.getEntityForRevision();
        MyRevisionEntity revEntity = auditViewEntryContainer.getRevEntity();
        RevisionType revisionType = auditViewEntryContainer.getRevisionType();

        AuditReader a = AuditReaderFactory.get(entityManager);
        Auditable previous = getPrevious(a, entityForRevision, revEntity);
        String newValueForAuditLog = revisionType == DEL ? StringUtils.EMPTY : entityForRevision.getNewValueForAuditLog();
        return AuditViewEntry
                .builder(revEntity, revisionType)
                .oldValue(previous == null ? StringUtils.EMPTY : previous.getNewValueForAuditLog())
                .value(newValueForAuditLog)
                .type(entityForRevision.getType())
                .name(entityForRevision.getNameForAuditLog())
                .editContextName(getContextName(auditViewEntryContainer.getEditContextId()))
                .relation(auditViewEntryContainer.getRelationName())
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

    private String getContextName(Integer editContextId) {
        if (editContextId == null) {
            return StringUtils.EMPTY;
        }
        ContextEntity contextEntity = contextRepository.find(editContextId);
        if (contextEntity == null) {
            return String.format("Context with id %d does not exist.", editContextId);
        }
        return contextEntity.getName();
    }
}
