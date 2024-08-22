package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PropertyTagDTO {

    private Integer id;
    private String name;
    // tagType is assumed to be global

    public PropertyTagDTO(PropertyTagEntity propertyTagEntity) {
        this.id = propertyTagEntity.getId();
        this.name = propertyTagEntity.getName();
    }
}
