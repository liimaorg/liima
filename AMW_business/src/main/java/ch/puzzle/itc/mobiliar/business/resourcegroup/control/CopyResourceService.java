package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.CopyResource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.GetCandidatesToCopyFromResourceUseCase;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroup;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class CopyResourceService implements GetCandidatesToCopyFromResourceUseCase {

    // note: we should not use the boundary here. this is a concious decision for simplicity (for now)
    @Inject
    private CopyResource copyResource;

    @Override
    public List<ResourceGroup> getCandidates(ResourceEntity resource) {
        return copyResource.loadResourceGroupsForType(resource.getResourceType().getId(), resource);
    }
}
