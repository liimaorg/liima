package ch.puzzle.itc.mobiliar.business.deploy.boundary;

import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

public interface DeploymentLogContentUseCase {

    String getContent(DeploymentLogContentCommand command) throws ValidationException;
}
