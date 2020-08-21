package ch.puzzle.itc.mobiliar.business.auditview.control;

import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntryContainer;
import ch.puzzle.itc.mobiliar.business.auditview.entity.Auditable;
import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextRepository;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import static org.hibernate.envers.RevisionType.DEL;

public abstract class AuditHandler {

    public static final String OBFUSCATED_VALUE = "*******";

    @PersistenceContext
    protected EntityManager entityManager;

    @Inject
    protected ContextRepository contextRepository;


    public abstract AuditViewEntry createAuditViewEntry(AuditViewEntryContainer auditViewEntryContainer);

    public AuditViewEntry createGenericAuditViewEntry(AuditViewEntryContainer auditViewEntryContainer) {
        Auditable entityForRevision = auditViewEntryContainer.getEntityForRevision();
        MyRevisionEntity revEntity = auditViewEntryContainer.getRevEntity();

        AuditReader reader = AuditReaderFactory.get(entityManager);
        Auditable previous = getPrevious(reader, entityForRevision, revEntity);
        return buildAuditViewEntry(auditViewEntryContainer, previous);
    }

    protected AuditViewEntry buildAuditViewEntry(AuditViewEntryContainer auditViewEntryContainer, Auditable previous) {
        Auditable entityForRevision = auditViewEntryContainer.getEntityForRevision();
        MyRevisionEntity revEntity = auditViewEntryContainer.getRevEntity();
        RevisionType revisionType = auditViewEntryContainer.getRevisionType();

        String value = getNewValueForAuditLog(entityForRevision, revisionType);
        String oldValue = getOldValueForAuditLog(previous);
        return AuditViewEntry
                .builder(revEntity, revisionType)
                .oldValue(oldValue)
                .value(value)
                .type(entityForRevision.getType())
                .name(entityForRevision.getNameForAuditLog())
                .editContextName(getContextName(auditViewEntryContainer.getEditContextId()))
                .relation(auditViewEntryContainer.getRelationName())
                .isObfuscatedValue(entityForRevision.isObfuscatedValue())
                .build();
    }

    protected String getOldValueForAuditLog(Auditable previous) {
        if (previous == null) {
            return StringUtils.EMPTY;
        }
        return previous.isObfuscatedValue() ? OBFUSCATED_VALUE : previous.getNewValueForAuditLog();
    }

    protected String getNewValueForAuditLog(Auditable entityForRevision, RevisionType revisionType) {
        if (revisionType == DEL) {
            return StringUtils.EMPTY;
        }
        return entityForRevision.isObfuscatedValue() ? OBFUSCATED_VALUE : entityForRevision.getNewValueForAuditLog();
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
