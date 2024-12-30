package ch.mobi.itc.mobiliar.rest.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement(name = "appAppServer")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppAppServerDTO {
    String appName;
    Integer appReleaseId;
    Integer appServerId;
    Integer appServerReleaseId;

    public AppAppServerDTO(String appName, Integer appReleaseId) {
        this.appName = appName;
        this.appServerId = appReleaseId;
    }
}
