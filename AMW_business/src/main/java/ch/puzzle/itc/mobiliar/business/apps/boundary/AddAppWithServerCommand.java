package ch.puzzle.itc.mobiliar.business.apps.boundary;

import ch.puzzle.itc.mobiliar.business.apps.validation.ValidAppName;
import lombok.Getter;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class AddAppWithServerCommand {

    @ValidAppName
    private final String appName;
    @NotNull
    private final Integer releaseId;
    @NotNull
    private final Integer appServerId;
    @NotNull
    private final Integer appServerReleaseId;

    public AddAppWithServerCommand(String appName, Integer releaseId, Integer appServerId, Integer appServerReleaseId) throws ValidationException {
        this.appName = appName;
        this.releaseId = releaseId;
        this.appServerId = appServerId;
        this.appServerReleaseId = appServerReleaseId;
        validate(this);
    }
}
