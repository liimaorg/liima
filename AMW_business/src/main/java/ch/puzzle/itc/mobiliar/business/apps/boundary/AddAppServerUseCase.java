package ch.puzzle.itc.mobiliar.business.apps.boundary;

import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

public interface AddAppServerUseCase {
    Integer add(AppServerCommand appServerCommand) throws NotFoundException, IllegalStateException;
}
