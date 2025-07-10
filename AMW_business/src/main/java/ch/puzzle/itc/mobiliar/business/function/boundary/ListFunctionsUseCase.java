package ch.puzzle.itc.mobiliar.business.function.boundary;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

import java.util.List;

public interface ListFunctionsUseCase {

    List<AmwFunctionEntity> functionsForResource(Integer id) throws NotFoundException;
    List<AmwFunctionEntity> functionsForResourceType(Integer id) throws NotFoundException;
}
