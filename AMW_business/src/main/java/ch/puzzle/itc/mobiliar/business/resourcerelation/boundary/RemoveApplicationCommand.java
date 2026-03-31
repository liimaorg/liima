package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import lombok.Getter;

import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class RemoveApplicationCommand {

    @NotNull(message = "App server resource id is required")
    private Integer appServerResourceId;
    @NotNull(message = "Relation id is required")
    private  Integer relationId;

    public RemoveApplicationCommand(Integer appServerResourceId, Integer relationId) {
        this.appServerResourceId = appServerResourceId;
        this.relationId = relationId;

        validate(this);
    }
}
