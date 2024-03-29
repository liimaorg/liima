package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

@Stateless
@AllArgsConstructor
@NoArgsConstructor
public class ListTagsUseCase {

    @Inject
    private PropertyTagEditingService propertyTagEditingService;

    public List<PropertyTagEntity> get() {
        return propertyTagEditingService.loadAllGlobalPropertyTagEntities(false);
    }
}
