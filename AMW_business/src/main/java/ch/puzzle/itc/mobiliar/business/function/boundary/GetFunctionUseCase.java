package ch.puzzle.itc.mobiliar.business.function.boundary;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

public interface GetFunctionUseCase {

    AmwFunctionEntity getFunction(Integer id) throws NotFoundException;
}
