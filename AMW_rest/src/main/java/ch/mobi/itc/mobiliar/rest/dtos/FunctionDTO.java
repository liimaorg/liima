package ch.mobi.itc.mobiliar.rest.dtos;


import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@XmlRootElement(name = "function")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionDTO {

    Integer id;
    String name;
    String implementation;
    Set<String> miks;
    Boolean definedOnResource;
    Boolean definedOnResourceType;

    public FunctionDTO(AmwFunctionEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.implementation = entity.getImplementation();
        this.definedOnResource = entity.isDefinedOnResource();
        this.definedOnResourceType = entity.isDefinedOnResourceType();
        this.miks = entity.getMikNames();
    }
}