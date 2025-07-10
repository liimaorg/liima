package ch.puzzle.itc.mobiliar.business.function.boundary;

import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

public interface DeleteFunctionUseCase {
    void deleteFunction(Integer id) throws ValidationException, NotFoundException;
}
