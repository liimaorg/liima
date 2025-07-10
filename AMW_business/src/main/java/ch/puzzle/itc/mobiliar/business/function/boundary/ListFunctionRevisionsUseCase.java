package ch.puzzle.itc.mobiliar.business.function.boundary;

import ch.puzzle.itc.mobiliar.business.template.entity.RevisionInformation;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

import java.util.List;

public interface ListFunctionRevisionsUseCase {

    List<RevisionInformation> getRevisions(Integer functionId) throws NotFoundException;
}
