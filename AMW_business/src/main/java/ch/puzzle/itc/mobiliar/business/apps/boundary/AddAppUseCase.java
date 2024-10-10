package ch.puzzle.itc.mobiliar.business.apps.boundary;

import ch.puzzle.itc.mobiliar.common.exception.*;


public interface AddAppUseCase {
    Integer add(AddAppCommand addAppCommand) throws NotFoundException, IllegalStateException;
}
