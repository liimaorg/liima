package ch.mobi.itc.mobiliar.rest.tags.usecases;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;

@Stateless
@AllArgsConstructor
@NoArgsConstructor
public class RemoveTagUseCase {

    @Inject
    private PropertyTagEditingService propertyTagEditingService;

    public void removeTag(int id) {
        try {
            propertyTagEditingService.deletePropertyTagById(id);
        } catch (Exception e) {
            // this is a workaround since {@link PropertyTagEditingService#deletePropertyTagById} does not handle
            // the case where removing a tag fails
            throw new NoResultException("Unable to remove tag");
        }
    }
}
