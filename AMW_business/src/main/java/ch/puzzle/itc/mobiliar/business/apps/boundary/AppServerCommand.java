package ch.puzzle.itc.mobiliar.business.apps.boundary;

import ch.puzzle.itc.mobiliar.business.apps.validation.ValidAppName;
import lombok.Getter;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class AppServerCommand {
    @ValidAppName
    private final String appServerName;

    @NotNull
    private final Integer releaseId;

    public AppServerCommand(String serverName, Integer releaseId) throws ValidationException {
        this.appServerName = serverName;
        this.releaseId = releaseId;
        validate(this);
    }
}
