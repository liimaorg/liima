package ch.puzzle.itc.mobiliar.business.utils;

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
    String value;
    long revision;
    RevisionType mode;

    public AuditViewEntry(MyRevisionEntity myRevisionEntity) {
        this.username = myRevisionEntity.getUsername();
        this.timestamp = myRevisionEntity.getTimestamp();
        this.revision = myRevisionEntity.getId();
    }

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
