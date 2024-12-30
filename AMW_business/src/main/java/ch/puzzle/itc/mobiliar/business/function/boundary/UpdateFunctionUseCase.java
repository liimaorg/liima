package ch.puzzle.itc.mobiliar.business.function.boundary;

import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

public interface UpdateFunctionUseCase {
    void update(UpdateFunctionCommand updateFunctionCommand) throws IllegalStateException, NotFoundException, ValidationException;
}
