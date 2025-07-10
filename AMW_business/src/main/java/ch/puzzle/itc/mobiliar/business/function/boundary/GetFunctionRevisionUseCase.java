package ch.puzzle.itc.mobiliar.business.function.boundary;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

public interface GetFunctionRevisionUseCase {
    AmwFunctionEntity getFunctionRevision(int id, int revisionId) throws NotFoundException;
}
