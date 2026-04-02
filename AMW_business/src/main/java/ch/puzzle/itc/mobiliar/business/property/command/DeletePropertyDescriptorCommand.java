package ch.puzzle.itc.mobiliar.business.property.command;

import lombok.Getter;

import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class DeletePropertyDescriptorCommand {

    @NotNull(message = "Property descriptor ID may not be null")
    private final Integer descriptorId;

    private final Integer resourceId;
    private final Integer resourceTypeId;
    private final boolean forceDelete;

    public DeletePropertyDescriptorCommand(Integer descriptorId, Integer resourceId, Integer resourceTypeId, boolean forceDelete) throws IllegalArgumentException {
        if (resourceId == null && resourceTypeId == null) {
            throw new IllegalArgumentException("Either resourceId or resourceTypeId must be provided");
        }
        if (resourceId != null && resourceTypeId != null) {
            throw new IllegalArgumentException("Only one of resourceId or resourceTypeId should be provided");
        }
        this.descriptorId = descriptorId;
        this.resourceId = resourceId;
        this.resourceTypeId = resourceTypeId;
        this.forceDelete = forceDelete;
        validate(this);
    }

    public boolean isForResource() {
        return resourceId != null;
    }
}
