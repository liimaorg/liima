package ch.puzzle.itc.mobiliar.business.apps.boundary;

import ch.puzzle.itc.mobiliar.business.apps.validation.ValidAppName;
import lombok.Getter;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class AddAppCommand {

    @ValidAppName
    private final String appName;

    @NotNull
    private final Integer releaseId;

    public AddAppCommand(String appName, Integer releaseId) throws ValidationException {
        this.appName = appName;
        this.releaseId = releaseId;
        validate(this);
    }

}
