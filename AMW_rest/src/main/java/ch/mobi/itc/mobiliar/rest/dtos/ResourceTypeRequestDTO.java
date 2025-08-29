package ch.mobi.itc.mobiliar.rest.dtos;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Request to add a new resource type")
public class ResourceTypeRequestDTO {
    @Schema(required = true)
    private String name;

    @Schema(required = false)
    private Integer parentId;
}
