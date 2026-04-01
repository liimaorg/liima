package ch.mobi.itc.mobiliar.rest.resources.propertyDescriptor;

import lombok.Getter;

import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class GetPropertyDescriptorCommand {

    @NotNull(message = "Property descriptor ID may not be null")
    private final Integer descriptorId;

    public GetPropertyDescriptorCommand(Integer descriptorId) {
        this.descriptorId = descriptorId;
        validate(this);
    }
}
