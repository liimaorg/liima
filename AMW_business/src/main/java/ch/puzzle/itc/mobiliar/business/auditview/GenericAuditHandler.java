package ch.puzzle.itc.mobiliar.business.auditview;

import ch.puzzle.itc.mobiliar.business.auditview.control.AuditHandler;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntryContainer;

import javax.ejb.Stateless;
import javax.inject.Named;

@Stateless
@Named("genericAuditHandler")
public class GenericAuditHandler extends AuditHandler {

    public AuditViewEntry createAuditViewEntry(AuditViewEntryContainer auditViewEntryContainer) {
        return super.createGenericAuditViewEntry(auditViewEntryContainer);
    }
}
