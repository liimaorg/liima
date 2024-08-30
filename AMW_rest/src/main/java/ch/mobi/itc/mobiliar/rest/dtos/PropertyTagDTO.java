package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "propertyTag")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
public class PropertyTagDTO {

    @NotNull(message = "PropertyTag name must not be null.")
    @NotEmpty(message = "PropertyTag name must not be empty.")
    private String name;
    @NotNull(message = "PropertyTag type must not be null.")
    @NotEmpty(message = "PropertyTag type must not be empty.")
    private String type;

    public PropertyTagDTO(PropertyTagEntity propertyTagEntity) {
        this.name = propertyTagEntity.getName();
        this.type = String.valueOf(propertyTagEntity.getTagType());
    }
}
