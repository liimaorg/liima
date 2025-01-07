package ch.puzzle.itc.mobiliar.business.function.boundary;

import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

public interface AddFunctionUseCase {
    Integer addForResource(AddFunctionCommand addFunctionCommand) throws IllegalStateException, NotFoundException, ValidationException;
    Integer addForResourceType(AddFunctionCommand addFunctionCommand) throws IllegalStateException, NotFoundException, ValidationException;
}
