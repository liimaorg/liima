package ch.puzzle.itc.mobiliar.business.function.boundary;
import ch.puzzle.itc.mobiliar.business.function.validation.ValidFunctionContent;
import ch.puzzle.itc.mobiliar.business.function.validation.ValidFunctionName;
import lombok.Getter;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import java.util.Set;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;


@Getter
public class AddFunctionCommand {

    @NotNull
    private final Integer resourceId;

    @ValidFunctionName
    private final String name;

    private final Set<String> miks;

    @ValidFunctionContent
    private final String content;

    public AddFunctionCommand(Integer resourceId, String name, Set<String> miks, String content) throws ValidationException {
        this.resourceId = resourceId;
        this.name = name;
        this.miks = miks;
        this.content = content;
        validate(this);
    }

}
