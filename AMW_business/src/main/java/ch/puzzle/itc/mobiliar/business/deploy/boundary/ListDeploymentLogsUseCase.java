package ch.puzzle.itc.mobiliar.business.deploy.boundary;

import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

import java.util.List;


public interface ListDeploymentLogsUseCase {

    /**
     * List the logs for a given deployment (by deployment id)
     * @param deploymentId
     */
    List<DeploymentLog> logsFor(Long deploymentId) throws NotFoundException;
}
