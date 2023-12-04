package ch.mobi.itc.mobiliar.rest.tags;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
class ListTagsUseCase {

    @Inject
    private PropertyTagEditingService propertyTagEditingService;

    public Object get() {
        return propertyTagEditingService.loadAllGlobalPropertyTagEntities(false);
    }
}
