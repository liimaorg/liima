package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import org.junit.Test;
import org.mockito.InjectMocks;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PropertyDTOTest {

    @InjectMocks
    PropertyDTO dto;

    @Test
    public void shouldReturnPropertyValueIfPropertyValueIsNotNull() {
        // given
        String propertyValue = "test";
        String defaultValue = "default";
        String context = "Global";
        ResourceEditProperty resourceEditProperty = new ResourceEditProperty("technicalKey", "displayName",
                propertyValue, "exampleValue", defaultValue, "propertyComment",
                true, true,false, null, true, "validationLogic",
                "mik", null, null, null, "propContName",
                "typeContName", null, null, null, null,null,
                null, null, null,null, null, null, null,null);
        // when
        dto = new PropertyDTO(resourceEditProperty, context);

        // then
        assertThat(dto.getValue(), is(propertyValue));
    }

    @Test
    public void shouldReturnDefaultValueIfPropertyValueIsNull() {
        // given
        String propertyValue = null;
        String defaultValue = "default";
        String context = "Global";
        ResourceEditProperty resourceEditProperty = new ResourceEditProperty("technicalKey", "displayName",
                propertyValue, "exampleValue", defaultValue, "propertyComment",
                true, true,false, null, true, "validationLogic",
                "mik", null, null, null, "propContName",
                "typeContName", null, null, null, null,null,
                null, null, null,null, null, null, null,null);
        // when
        dto = new PropertyDTO(resourceEditProperty, context);

        // then
        assertThat(dto.getValue(), is(defaultValue));
    }
}