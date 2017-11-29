package ch.puzzle.itc.mobiliar.business.utils;

import lombok.Builder;
import lombok.Getter;
import org.hibernate.envers.RevisionType;

@Getter
@Builder
public class AuditViewEntry {
    long timestamp;
    String type; // Property, Resource, ...
    String name; // PropertyName, ...
    String username;
    String value;
    long revision;
    RevisionType mode;

    public String getModeAsString() {
        return this.getMode().name();
    }
}
