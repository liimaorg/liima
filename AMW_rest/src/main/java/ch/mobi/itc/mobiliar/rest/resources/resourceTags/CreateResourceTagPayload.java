package ch.mobi.itc.mobiliar.rest.resources.resourceTags;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateResourceTagPayload {

    private String label;
    private Date tagDate;

}
