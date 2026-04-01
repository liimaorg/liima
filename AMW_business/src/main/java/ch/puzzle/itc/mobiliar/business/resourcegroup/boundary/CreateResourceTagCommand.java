package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import ch.puzzle.itc.mobiliar.business.configurationtag.boundary.TagConfiguration;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.Date;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class CreateResourceTagCommand {

    @NotNull(message = "resourceId may not be null")
    private final Integer resourceId;

    @NotNull(message = "label may not be null")
    private final String label;
    
    @NotNull(message = "tagDate may not be null")
    private final Date tagDate;


    public CreateResourceTagCommand(Integer resourceId, String label, Date tagDate) {
        this.resourceId = resourceId;
        this.label = label;
        this.tagDate = tagDate;
        
        validate(this);
    }

    public TagConfiguration toTagConfiguration() {
        return new TagConfiguration(resourceId, label, tagDate);
    }
}
