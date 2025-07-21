package ch.puzzle.itc.mobiliar.business.function.boundary;

import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

public interface OverwriteFunctionUseCase {
    int overwriteForResource(OverwriteFunctionCommand overwriteFunctionCommand) throws IllegalStateException, AMWException;
    int overwriteForResourceType(OverwriteFunctionCommand overwriteFunctionCommand) throws IllegalStateException, NotFoundException, ValidationException;
}
