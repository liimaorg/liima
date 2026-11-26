package ch.mobi.itc.mobiliar.rest.resources;

import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyValueLocator;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyValueLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyLocationsRestTest {

    @Mock
    PropertyValueLocator locator;

    @InjectMocks
    PropertyLocationsRest rest;

    @Test
    void rejectsMissingOrBlankValues() {
        for (String value : new String[]{null, "", " \t "}) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rest.getLocations(value).getStatus());
        }
        verifyNoInteractions(locator);
    }

    @Test
    void returnsLocationsWithoutChangingTheSearchValue() {
        String value = " host%_ ";
        PropertyValueLocation location = org.mockito.Mockito.mock(PropertyValueLocation.class);
        List<PropertyValueLocation> locations = Collections.singletonList(location);
        when(locator.findByValue(value)).thenReturn(locations);

        Response response = rest.getLocations(value);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(locations, response.getEntity());
    }

    @Test
    void returnsAnEmptyListWhenNoDefinitionsMatch() {
        when(locator.findByValue("missing")).thenReturn(Collections.emptyList());

        Response response = rest.getLocations("missing");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(Collections.emptyList(), response.getEntity());
    }
}
