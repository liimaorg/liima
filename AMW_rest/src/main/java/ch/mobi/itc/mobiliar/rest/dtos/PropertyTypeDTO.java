package ch.mobi.itc.mobiliar.rest.dtos;


import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "propertyType")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTypeDTO {

    private Integer id;
    @NotNull(message = "PropertyType name must not be null.")
    @NotEmpty(message = "PropertyType name must not be empty.")
    private String name;
    private boolean encrypted;
    @NotNull(message = "PropertyType validation regex must not be null.")
    @NotEmpty(message = "PropertyType validation regex must not be empty.")
    private String validationRegex;
    private List<PropertyTagDTO> propertyTags;

    public PropertyTypeDTO(PropertyTypeEntity propertyTypeEntity) {
        this.id = propertyTypeEntity.getId();
        this.name = propertyTypeEntity.getPropertyTypeName();
        this.encrypted = propertyTypeEntity.isEncrypt();
        this.validationRegex = propertyTypeEntity.getValidationRegex();
        this.propertyTags = new ArrayList<>();
        for (PropertyTagEntity propertyTagEntity : propertyTypeEntity.getPropertyTags()) {
            this.propertyTags.add(new PropertyTagDTO(propertyTagEntity));
        }

    }
}
