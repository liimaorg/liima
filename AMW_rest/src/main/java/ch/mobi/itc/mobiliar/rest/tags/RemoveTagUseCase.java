package ch.mobi.itc.mobiliar.rest.tags;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;

@Stateless
@AllArgsConstructor
@NoArgsConstructor
class RemoveTagUseCase {

    @Inject
    private PropertyTagEditingService propertyTagEditingService;

    void removeTag(int id) {
        try {
            propertyTagEditingService.deletePropertyTagById(id);
        } catch (Exception e) {
            // this is a workaround since {@link PropertyTagEditingService#deletePropertyTagById} does not handle
            // the case where removing a tag fails
            throw new NoResultException("Unable to remove tag");
        }
    }
}
