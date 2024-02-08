package ch.puzzle.itc.mobiliar.business.deploy.boundary;

import ch.puzzle.itc.mobiliar.business.deploy.validation.ValidFileName;
import lombok.Getter;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class DeploymentLogContentCommand {

    @NotNull
    private Integer id;
    @ValidFileName
    private String fileName;

    public DeploymentLogContentCommand(Integer id, String fileName) throws ValidationException {
        this.id = id;
        this.fileName = fileName;
        validate(this);
    }
}
