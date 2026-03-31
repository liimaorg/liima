package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import lombok.Getter;

import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class ResourceIdCommand {

    @NotNull(message = "is a required parameter")
    private final Integer resourceId;

    public ResourceIdCommand(Integer resourceId) {
        this.resourceId = resourceId;
        validate(this);
    }
}
