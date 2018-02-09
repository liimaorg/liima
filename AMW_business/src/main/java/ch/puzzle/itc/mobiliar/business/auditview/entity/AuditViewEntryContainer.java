package ch.puzzle.itc.mobiliar.business.auditview.entity;

import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import lombok.Data;
import org.hibernate.envers.RevisionType;

@Data
public class AuditViewEntryContainer {
    Auditable entityForRevision;
    MyRevisionEntity revEntity;
    RevisionType revisionType;

    String relationName;
    Integer editContextId = null;

    // used to mask encrypted values
    boolean obfuscated = false;

    public AuditViewEntryContainer(Object[] enversTriple) {
       this.entityForRevision = (Auditable) enversTriple[0];
       this.revEntity = (MyRevisionEntity) enversTriple[1];
       this.revisionType = (RevisionType) enversTriple[2];
    }
}
