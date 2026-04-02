package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import ch.puzzle.itc.mobiliar.common.exception.AMWException;

public interface CopyFromResourceUseCase {

    void copyFromResource(CopyFromResourceCommand command) throws AMWException;
}
