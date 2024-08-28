package ch.mobi.itc.mobiliar.rest.dtos;


import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTypeDTO {

    private Integer id;
    private String name;
    private boolean encrypted;
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
