package ch.puzzle.itc.mobiliar.business.configurationtag.boundary;

import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

import java.util.Date;
import java.util.List;

public interface CreateTagUseCase {


    /**
     * @param tagConfiguration - contains resourceId, tag and date
     * @return the created tag
     */
    ResourceTagEntity createTag(TagConfiguration tagConfiguration) throws NotFoundException;

    /**
     *
     * @deprecated use createTag(tagConfiguration) instead
     * added for backwards compatibility
     * @param resourceId - id of the resource
     * @param tag - tag label
     * @param date - tag date
     * @return the created tag
     */
    @Deprecated
    ResourceTagEntity tagConfiguration(int resourceId, String tag, Date date);



    /**
     * added for backwards compatibility
     * @param resource - resource to load tags for
     * @return list of tags for the resource
     */
    List<ResourceTagEntity> loadTagLabelsForResource(ResourceEntity resource);
}