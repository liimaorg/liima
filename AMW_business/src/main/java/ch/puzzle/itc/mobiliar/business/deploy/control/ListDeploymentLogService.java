package ch.puzzle.itc.mobiliar.business.deploy.control;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLog;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.ListDeploymentLogsUseCase;
import lombok.NonNull;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class ListDeploymentLogService implements ListDeploymentLogsUseCase {

    @Inject
    private DeploymentBoundary deploymentBoundary;

    @Override
    public List<DeploymentLog> logsFor(@NonNull Integer deploymentId) {
        deploymentBoundary.getDeploymentById(deploymentId); // check that the deployment with the given id exists. throws an exception if no entity is found!
        return Arrays.stream(deploymentBoundary.getLogFileNames(deploymentId))
                     .map(fileName -> new DeploymentLog(deploymentId, fileName))
                     .collect(Collectors.toList());
    }
}
