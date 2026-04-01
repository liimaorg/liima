package ch.puzzle.itc.mobiliar.business.configurationtag.boundary;

import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface ListResourceTagsUseCase {

    List<ResourceTagEntity> getTags(@NotNull(message = "must not be null") Integer resourceId) throws NotFoundException;

    List<ResourceTagEntity> getTags(@NotNull(message = "must not be null") ResourceEntity resourceEntity);
}
