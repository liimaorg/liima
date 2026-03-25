package ch.mobi.itc.mobiliar.rest.resources.resourceTags;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceTagDTO;
import ch.puzzle.itc.mobiliar.business.configurationtag.boundary.TagConfiguration;
import lombok.Getter;

import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;
import static ch.puzzle.itc.mobiliar.business.utils.ValidationHelper.validateNotNull;
import static ch.puzzle.itc.mobiliar.business.utils.ValidationHelper.validateNotNullOrEmpty;

@Getter
public class CreateResourceTagCommand {

    @NotNull(message = "may not be null")
    private final Integer resourceId;

    @NotNull(message = "may not be null")
    private final ResourceTagDTO resourceTag;

    public CreateResourceTagCommand(Integer resourceId, ResourceTagDTO resourceTag) {
        this.resourceId = resourceId;
        this.resourceTag = resourceTag;
        validate(this);
        validateNotNullOrEmpty(resourceTag.getLabel(), new IllegalArgumentException("Label must not be null or empty"));
        validateNotNull(resourceTag.getTagDate(), new IllegalArgumentException("Tag date must not be null"));
    }

    public TagConfiguration toTagConfiguration() {
        return new TagConfiguration(resourceId, resourceTag.getLabel(), resourceTag.getTagDate());
    }
}
