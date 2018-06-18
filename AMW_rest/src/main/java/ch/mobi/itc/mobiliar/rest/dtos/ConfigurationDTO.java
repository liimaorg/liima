package ch.mobi.itc.mobiliar.rest.dtos;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonAutoDetect()
public class ConfigurationDTO {
    private String key;
    private String env;
    private String value;
    private String defaultValue;
}
