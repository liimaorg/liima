package ch.puzzle.itc.mobiliar.business.deploy.control;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLog;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.ListDeploymentLogsUseCase;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import lombok.NonNull;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class ListDeploymentLogService implements ListDeploymentLogsUseCase {

    @Inject
    private DeploymentBoundary deploymentBoundary;

    @Override
    public List<DeploymentLog> logsFor(@NonNull Integer deploymentId) throws NotFoundException {
        List<DeploymentLog> deploymentLogFiles = Arrays.stream(deploymentBoundary.getLogFileNames(deploymentId))
                .map(fileName -> new DeploymentLog(deploymentId, fileName))
                .collect(Collectors.toList());
        if (deploymentLogFiles.isEmpty()) throw new NotFoundException(String.format("No deployment log files found for deployment '%d'", deploymentId));
        return deploymentLogFiles;
    }
}
