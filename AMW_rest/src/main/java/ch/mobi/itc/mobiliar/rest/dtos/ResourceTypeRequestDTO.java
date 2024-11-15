package ch.mobi.itc.mobiliar.rest.dtos;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(description = "Request to add a new resource type")
public class ResourceTypeRequestDTO {
    @ApiModelProperty(required = true)
    private String newResourceTypeName;

    @ApiModelProperty(required = false)
    private Integer parentId;
}
