package ch.puzzle.itc.mobiliar.business.auditview.entity;

import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import lombok.*;
import org.hibernate.envers.RevisionType;

@Getter
@Builder(builderMethodName = "hiddenBuilder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class AuditViewEntry {
    long timestamp;
    String type; // Property, Resource, ...
    String name; // PropertyName, ...
    String username;
    String oldValue;
    String value;
    long revision;
    RevisionType mode;
    String editContextName;
    String relation; // consumed resource, ...

    public String getModeAsString() {
        return this.getMode().name();
    }

    public static AuditViewEntryBuilder builder(MyRevisionEntity revisionEntity, RevisionType revisionType) {
                return hiddenBuilder()
                        .username(revisionEntity.getUsername())
                        .timestamp(revisionEntity.getTimestamp())
                        .revision(revisionEntity.getId())
                        .mode(revisionType);
    }
}
