package ch.puzzle.itc.mobiliar.business.releasing.boundary;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResourceEntityDto implements Comparable<ResourceEntityDto> {

    private Integer id;

    private String name;

    private ResourceTypeEntityDto resourceType;

    @Override
    public int compareTo(ResourceEntityDto o) {
        if (getName() == null) {
            return -1;
        }
        if (o == null) {
            return 1;
        }
        int c = getName().compareToIgnoreCase(o.getName());
        if (c == 0) {
            return getId() != null ? getId().compareTo(o.getId()) : o.getId() == null ? 0 : -1;
        }
        return c;
    }
}
