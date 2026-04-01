package ch.mobi.itc.mobiliar.rest.resources.propertyDescriptor;

import ch.mobi.itc.mobiliar.rest.dtos.PropertyDescriptorDTO;
import lombok.Getter;

import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class CreatePropertyDescriptorCommand {

    private final Integer resourceId;
    private final Integer resourceTypeId;

    @NotNull(message = "Property descriptor data may not be null")
    private final PropertyDescriptorDTO descriptorDTO;

    public CreatePropertyDescriptorCommand(Integer resourceId, Integer resourceTypeId, PropertyDescriptorDTO descriptorDTO) {
        if (resourceId == null && resourceTypeId == null) {
            throw new IllegalArgumentException("Either resourceId or resourceTypeId must be provided");
        }
        if (resourceId != null && resourceTypeId != null) {
            throw new IllegalArgumentException("Only one of resourceId or resourceTypeId should be provided");
        }
        this.resourceId = resourceId;
        this.resourceTypeId = resourceTypeId;
        this.descriptorDTO = descriptorDTO;
        validate(this);
    }

    public boolean isForResource() {
        return resourceId != null;
    }

    public boolean isForResourceType() {
        return resourceTypeId != null;
    }
}
