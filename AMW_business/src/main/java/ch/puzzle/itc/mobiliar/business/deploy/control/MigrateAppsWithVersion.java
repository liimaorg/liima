package ch.puzzle.itc.mobiliar.business.deploy.control;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.entity.ApplicationWithVersionEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.CustomFilter;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.util.Tuple;

public class MigrateAppsWithVersion {

    @Inject
    private DeploymentBoundary deploymentBoundary;

    @Inject
    private ResourceRepository resourceRepository;

    @Inject
    protected EntityManager em;

    public void migrate() {
        Tuple<Set<DeploymentEntity>, Integer> result = deploymentBoundary.getFilteredDeployments(0, 1000, new LinkedList<CustomFilter>() , null, null, null);
        for(DeploymentEntity deployment : result.getA()) {

            if(deployment.getApplicationsWithVersionOld() == null || deployment.getApplicationsWithVersionOld().equals("")) {
                continue;
            }

            List<ApplicationWithVersion> oldApps = deployment.getApplicationsWithVersionOldAsList();
            deployment.setApplicationsWithVersion(new HashSet<>());
            for(ApplicationWithVersion oldApp : oldApps) {
                ResourceEntity currentApp = resourceRepository.find(oldApp.getApplicationId());
                ApplicationWithVersionEntity newApp = new ApplicationWithVersionEntity();
                newApp.setDeployment(deployment);
                newApp.setVersion(oldApp.getVersion());
                if(currentApp != null) {
                    newApp.setApplication(currentApp);
                }
                else {
                    newApp.setExApplicationId(oldApp.getApplicationId());
                }
                deployment.getApplicationsWithVersion().add(newApp);
            }

            em.merge(deployment);
        }
    }
}
