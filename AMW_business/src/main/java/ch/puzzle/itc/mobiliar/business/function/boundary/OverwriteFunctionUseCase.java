package ch.puzzle.itc.mobiliar.business.function.boundary;

import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

public interface OverwriteFunctionUseCase {
    Integer overwriteForResource(OverwriteFunctionCommand overwriteFunctionCommand) throws IllegalStateException, NotFoundException, ValidationException;
    Integer overwriteForResourceType(OverwriteFunctionCommand overwriteFunctionCommand) throws IllegalStateException, NotFoundException, ValidationException;
}
