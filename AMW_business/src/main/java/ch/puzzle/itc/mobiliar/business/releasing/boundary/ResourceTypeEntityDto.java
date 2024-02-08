package ch.puzzle.itc.mobiliar.business.releasing.boundary;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResourceTypeEntityDto implements Comparable<ResourceTypeEntityDto> {

    private Integer id;

    private String name;

    @Override
    public int compareTo(ResourceTypeEntityDto o) {
        if (name == null) {
            return -1;
        }
        if (o == null) {
            return 1;
        }
        return name.compareToIgnoreCase(o.getName());
    }
}
