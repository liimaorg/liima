package ch.puzzle.itc.mobiliar.business.apps.boundary;

import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;


public interface AddAppWithServerUseCase {
    Integer add(AddAppWithServerCommand addAppWithServerCommand) throws NotFoundException, IllegalStateException;
}
