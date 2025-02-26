package ch.puzzle.itc.mobiliar.business.apps.boundary;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceWithRelations;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.util.Tuple;

import java.util.List;

public interface ListAppsUseCase {

    List<ResourceWithRelations> appsFor(String filter, Integer releaseId) throws NotFoundException;

}
