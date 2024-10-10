package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "app")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppDTO {
    Integer id;
    String name;
    ReleaseDTO release;

    public AppDTO(ResourceEntity app) {
        this.id = app.getId();
        this.name = app.getName();
        if (app.getRelease() != null) {
            this.release = new ReleaseDTO(app.getRelease().getId(), app.getRelease().getName());
        }
    }
}
