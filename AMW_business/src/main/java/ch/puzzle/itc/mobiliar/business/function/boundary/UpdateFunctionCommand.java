package ch.puzzle.itc.mobiliar.business.function.boundary;

import ch.puzzle.itc.mobiliar.business.function.validation.ValidFunctionContent;
import lombok.Getter;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;


@Getter
public class UpdateFunctionCommand {

    @NotNull
    private final Integer id;

    @ValidFunctionContent
    private final String content;

    public UpdateFunctionCommand(Integer id, String content) throws ValidationException {
        this.id = id;
        this.content = content;
        validate(this);
    }

}
