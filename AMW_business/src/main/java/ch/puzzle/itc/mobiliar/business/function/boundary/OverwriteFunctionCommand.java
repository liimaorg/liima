package ch.puzzle.itc.mobiliar.business.function.boundary;

import ch.puzzle.itc.mobiliar.business.function.validation.ValidFunctionContent;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class OverwriteFunctionCommand {

    @NotNull
    private final Integer id;

    @ValidFunctionContent
    private final String content;

    @NotNull
    private final Integer resourceId;

    public OverwriteFunctionCommand(Integer resourceId, Integer functionId, String content) {
        this.id = functionId;
        this.content = content;
        this.resourceId = resourceId;
    }
}
