package ch.puzzle.itc.mobiliar.business.deploy.control;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLogContentCommand;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLogContentUseCase;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@Stateless
public class LogContentService implements DeploymentLogContentUseCase {

    @Inject
    private DeploymentBoundary deploymentBoundary;

    @Override
    public String getContent(DeploymentLogContentCommand command) throws ValidationException {
        List<String> availableLogFiles = Arrays.asList(deploymentBoundary.getLogFileNames(command.getId()));
        if (!availableLogFiles.contains(command.getFileName())) throw new ValidationException("filename not found");

        return deploymentBoundary.getDeploymentLog(command.getFileName());
    }
}
