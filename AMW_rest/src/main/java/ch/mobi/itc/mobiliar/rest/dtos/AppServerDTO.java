package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceWithRelations;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement(name = "appServer")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppServerDTO {

    Integer id;
    String name;
    Boolean deletable;
    String runtimeName;
    ReleaseDTO release;
    List<AppDTO> relatedResources;

    public AppServerDTO(ResourceWithRelations appServer) {
        this.id = appServer.getResource().getId();
        this.name = appServer.getResource().getName();
        this.deletable = appServer.getResource().isDeletable();
        this.runtimeName =  appServer.getResource().getRuntime() != null ? appServer.getResource().getRuntime().getName() : "";
        if (appServer.getResource().getRelease() != null) {
            this.release = new ReleaseDTO(appServer.getResource().getId(), appServer.getResource().getName());
        }

        this.relatedResources = new ArrayList<>();
        for (ResourceEntity app : appServer.getRelatedResources()) {
            this.relatedResources.add(new AppDTO(app));
        }


    }
}
