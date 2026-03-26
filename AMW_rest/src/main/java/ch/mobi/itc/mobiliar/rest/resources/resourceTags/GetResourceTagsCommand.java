package ch.mobi.itc.mobiliar.rest.resources.resourceTags;

import lombok.Getter;

import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class GetResourceTagsCommand {

    @NotNull(message = "must not be null")
    private final Integer resourceId;

    public GetResourceTagsCommand(Integer resourceId) {
        this.resourceId = resourceId;
        validate(this);
    }
}
