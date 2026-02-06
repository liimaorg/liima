package ch.mobi.itc.mobiliar.rest.dtos;

import lombok.Data;
import java.util.List;

@Data
public class PropertyBulkUpdateDTO {
    private List<PropertyDTO> updates;
    private List<PropertyDTO> resets;
}
