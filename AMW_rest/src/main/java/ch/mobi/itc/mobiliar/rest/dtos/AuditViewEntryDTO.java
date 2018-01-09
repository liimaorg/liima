package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry;
import lombok.Data;
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
public class AuditViewEntryDTO {
    Long timestamp;
    String type; // Property, Resource, ...
    String name; // PropertyName, ...
    String username;
    String oldValue;
    String value;
    long revision;
    String mode;
    String editContextName;

    public AuditViewEntryDTO(AuditViewEntry entry) {
        this.timestamp = entry.getTimestamp();
        this.type = entry.getType();
        this.name = entry.getName();
        this.username = entry.getUsername();
        this.oldValue = entry.getOldValue();
        this.value = entry.getValue();
        this.revision = entry.getRevision();
        this.mode = entry.getModeAsString();
        this.editContextName = entry.getEditContextName();
    }
}
