package ch.puzzle.itc.mobiliar.business.deploy.boundary;

import ch.puzzle.itc.mobiliar.business.deploy.validation.ValidFileName;
import lombok.Getter;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class DeploymentLogContentCommand {

    @NotNull
    private final Integer id;
    @ValidFileName
    private final String filename;

    public DeploymentLogContentCommand(Integer id, String filename) throws ValidationException {
        this.id = id;
        this.filename = filename;
        validate(this);
    }
}
