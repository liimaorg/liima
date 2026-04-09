package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import lombok.Getter;

import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class CopyFromResourceCommand {

    @NotNull(message = "is a required parameter")
    private Integer targetResourceId;

    @NotNull(message = "is a required parameter")
    private Integer originResourceId;

    public CopyFromResourceCommand(Integer targetResourceId, Integer originResourceId) {
        this.targetResourceId = targetResourceId;
        this.originResourceId = originResourceId;
        validate(this);
    }
}
