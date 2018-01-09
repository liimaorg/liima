package ch.puzzle.itc.mobiliar.business.auditview.entity;

import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.envers.RevisionType;

@Getter
@Builder(builderMethodName = "hiddenBuilder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
