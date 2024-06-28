package ch.puzzle.itc.mobiliar.business.deploy.control;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.*;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Stateless
public class LogContentService implements DeploymentLogContentUseCase {

    @Inject
    private DeploymentBoundary deploymentBoundary;

    @Override
    public DeploymentLog getContent(DeploymentLogContentCommand command) throws ValidationException, IOException {
        List<String> availableLogFiles = Arrays.asList(deploymentBoundary.getLogFileNames(command.getId()));
        if (!availableLogFiles.contains(command.getFilename()))
            throw new ValidationException(String.format("file %s not found!", command.getFilename()));

        return new DeploymentLog(command.getId(),
                command.getFilename(),
                deploymentBoundary.getDeploymentLog(command.getFilename()));
    }
}
