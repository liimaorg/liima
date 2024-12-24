package ch.puzzle.itc.mobiliar.business.function.boundary;
import ch.puzzle.itc.mobiliar.business.function.validation.ValidFunctionContent;
import ch.puzzle.itc.mobiliar.business.function.validation.ValidFunctionName;
import lombok.Getter;

import javax.validation.ValidationException;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;


@Getter
public class AddFunctionCommand {

    @ValidFunctionName
    private final String name;

    private final String[] miks;

    @ValidFunctionContent
    private final String content;

    public AddFunctionCommand(String name, String[] miks, String content) throws ValidationException {
        this.name = name;
        this.miks = miks;
        this.content = content;
        validate(this);
    }

}
