package ch.puzzle.itc.mobiliar.business.auditview.entity;

import lombok.Builder;
import lombok.Getter;
import org.hibernate.envers.RevisionType;

@Getter
@Builder
public class AuditViewEntry {
    long timestamp;
    String type; // Property, Resource, ...
    String user;
    String oldValue;
    String value;
    long revision;
    RevisionType mode;
}
