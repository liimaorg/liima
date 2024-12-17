package ch.puzzle.itc.mobiliar.business.function.boundary;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;

import java.util.List;

public interface ListFunctionsUseCase {

    List<AmwFunctionEntity> functionsForResource(Integer id);
    List<AmwFunctionEntity> functionsForResourceType(Integer id);
}
