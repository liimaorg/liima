package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PropertyTagDTO {

    private String name;
    private String type;

    public PropertyTagDTO(PropertyTagEntity propertyTagEntity) {
        this.name = propertyTagEntity.getName();
        this.type = String.valueOf(propertyTagEntity.getTagType());
    }
}
