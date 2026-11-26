package ch.mobi.itc.mobiliar.rest.settings;

import ch.mobi.itc.mobiliar.rest.dtos.PropertyTypeDTO;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyTypeService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Arrays;

import static javax.ws.rs.core.Response.Status.CREATED;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyTypesRestTest {

    @InjectMocks
    private PropertyTypesRest rest;

    @Mock
    private PropertyTypeService propertyTypeService;

    @Test
    void shouldAcceptAValidValidationRegex() throws Exception {
        when(propertyTypeService.create(any(PropertyTypeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Response response = rest.addPropertyType(propertyType("^[a-z]+$"));

        assertEquals(CREATED.getStatusCode(), response.getStatus());
        verify(propertyTypeService).create(any(PropertyTypeEntity.class));
    }

    @Test
    void shouldRejectAnInvalidValidationRegex() {
        assertThrows(ValidationException.class,
                () -> rest.addPropertyType(propertyType("[")));
    }

    @Test
    void shouldRejectUnsupportedPatternsOnCreateAndUpdate() {
        for (String regex : Arrays.asList("(?=a)a", "(?<=a)b", "(a)\\1", null, "")) {
            assertThrows(ValidationException.class,
                    () -> rest.addPropertyType(propertyType(regex)));
            assertThrows(ValidationException.class,
                    () -> rest.updatePropertyType(42, propertyType(regex)));
        }
        verifyNoInteractions(propertyTypeService);
    }

    private PropertyTypeDTO propertyType(String validationRegex) {
        PropertyTypeDTO request = new PropertyTypeDTO();
        request.setName("test");
        request.setValidationRegex(validationRegex);
        request.setPropertyTags(Collections.emptyList());
        return request;
    }
}
