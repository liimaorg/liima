package ch.puzzle.itc.mobiliar.business.auditview.control;

import ch.puzzle.itc.mobiliar.business.auditview.GenericAuditHandler;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntryContainer;
import ch.puzzle.itc.mobiliar.business.auditview.entity.Auditable;
import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextRepository;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import org.hibernate.envers.RevisionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class GenericAuditViewHandlerTest {

    @Mock
    protected EntityManager entityManager;

    @Mock
    protected ContextRepository contextRepository;

    @InjectMocks
    AuditHandler genericAuditHandler = new GenericAuditHandler();

    @Test
    public void shouldObfuscateValues() {
        // given
        Auditable entityForRevision = new PropertyDescriptorEntity();
        MyRevisionEntity revEntity = new MyRevisionEntity();
        RevisionType revisionType = RevisionType.MOD;
        Object[] enversTriple = {entityForRevision, revEntity, revisionType};

        AuditViewEntryContainer container = new AuditViewEntryContainer(enversTriple);
        container.setObfuscated(true);

        // when
        AuditViewEntry auditViewEntryWithObfuscatedValues = genericAuditHandler.buildAuditViewEntry(container, entityForRevision);

        // then
        assertThat(auditViewEntryWithObfuscatedValues.getValue(), is(AuditHandler.OBFUSCATED_VALUE));
        assertThat(auditViewEntryWithObfuscatedValues.getOldValue(), is(AuditHandler.OBFUSCATED_VALUE));
    }

}
