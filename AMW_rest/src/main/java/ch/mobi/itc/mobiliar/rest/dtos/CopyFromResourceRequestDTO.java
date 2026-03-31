package ch.mobi.itc.mobiliar.rest.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "copyFromResourceRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CopyFromResourceRequestDTO {

    @NotNull(message = "Target resource ID must not be null")
    @Schema(description = "Target resource ID (to)", required = true)
    private Integer targetResourceId;

    @NotNull(message = "Origin resource ID must not be null")
    @Schema(description = "Origin resource ID (from)", required = true)
    private Integer originResourceId;
}
