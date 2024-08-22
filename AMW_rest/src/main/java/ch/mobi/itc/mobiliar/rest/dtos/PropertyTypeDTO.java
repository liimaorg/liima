package ch.mobi.itc.mobiliar.rest.dtos;


import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class PropertyTypeDTO {

    private Integer id;
    private Long version;
    private String name;
    private boolean encrypted;
    private String validationRegex;
    private List<PropertyTagDTO> tags;

    public PropertyTypeDTO(PropertyTypeEntity propertyTypeEntity) {
        this.id = propertyTypeEntity.getId();
        this.version = propertyTypeEntity.getV();
        this.name = propertyTypeEntity.getPropertyTypeName();
        this.encrypted = propertyTypeEntity.isEncrypt();
        this.validationRegex = propertyTypeEntity.getValidationRegex();
        this.tags = propertyTypeEntity.getPropertyTags().stream().map(PropertyTagDTO::new).collect(Collectors.toList());
    }
}
