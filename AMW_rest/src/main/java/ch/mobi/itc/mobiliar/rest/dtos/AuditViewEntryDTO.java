package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.utils.AuditViewEntry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@Getter
@XmlRootElement(name = "auditViewEntry")
@XmlAccessorType(XmlAccessType.PROPERTY)
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"environment"})
public class AuditViewEntryDTO {
    Long timestamp;
    String type; // Property, Resource, ...
    String name; // PropertyName, ...
    String username;
    String value;
    long revision;
    String mode;
    String environment;

    public AuditViewEntryDTO(AuditViewEntry entry, String environment) {
        this.timestamp = entry.getTimestamp();
        this.type = entry.getType();
        this.name = entry.getName();
        this.username = entry.getUsername();
        this.value = entry.getValue();
        this.revision = entry.getRevision();
        this.mode = entry.getModeAsString();
        this.environment = environment;
    }
}
