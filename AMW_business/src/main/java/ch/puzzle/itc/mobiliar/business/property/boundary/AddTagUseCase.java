package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType;
import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.ejb.Stateless;
import javax.inject.Inject;


@Stateless
@AllArgsConstructor
@NoArgsConstructor
public class AddTagUseCase {

    @Inject
    PropertyTagEditingService propertyTagEditingService;

    public PropertyTagEntity addTag(@NonNull TagCommand tagCommand) throws ValidationException {
        PropertyTagEntity propertyTagEntity = new PropertyTagEntity(tagCommand.getName(), PropertyTagType.GLOBAL);
        try {
            return propertyTagEditingService.addPropertyTag(propertyTagEntity);
        } catch (AMWRuntimeException e) {
            // the runtime exception is caught here to provide the user with nice error messages.
            throw new ValidationException("Tag with name " + tagCommand.getName() + " already exists");
        }
    }
}
