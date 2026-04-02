package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroup;

import java.util.List;

public interface GetCandidatesToCopyFromResourceUseCase {

    List<ResourceGroup> getCandidates(ResourceEntity resource);
}
