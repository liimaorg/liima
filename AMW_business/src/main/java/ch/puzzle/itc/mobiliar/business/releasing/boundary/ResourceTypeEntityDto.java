package ch.puzzle.itc.mobiliar.business.releasing.boundary;

import lombok.Getter;
import lombok.Setter;

public class ResourceTypeEntityDto implements Comparable<ResourceTypeEntityDto> {
    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String name;

    public ResourceTypeEntityDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

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
