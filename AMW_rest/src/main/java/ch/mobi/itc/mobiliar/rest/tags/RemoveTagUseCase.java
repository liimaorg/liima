package ch.mobi.itc.mobiliar.rest.tags;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
class RemoveTagUseCase {

    @Inject
    private PropertyTagEditingService propertyTagEditingService;

    void removeTag(int id) {
        try {
            propertyTagEditingService.deletePropertyTagById(id);
        } catch (Exception e) {
            // this is a workaround since {@link PropertyTagEditingService#deletePropertyTagById} does not handle
            // the case where removing a tag fails
            e.printStackTrace();
            throw new IllegalArgumentException("Unable to remove tag with with id " + id);
        }

    }
}
