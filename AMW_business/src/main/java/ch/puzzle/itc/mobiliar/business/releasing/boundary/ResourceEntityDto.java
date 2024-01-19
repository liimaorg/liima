package ch.puzzle.itc.mobiliar.business.releasing.boundary;

import lombok.Getter;
import lombok.Setter;

public class ResourceEntityDto implements Comparable<ResourceEntityDto> {
    @Getter
    @Setter
    private Integer id;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private ResourceTypeEntityDto resourceType;

    public ResourceEntityDto(Integer id, String name, ResourceTypeEntityDto resourceType) {
        this.id = id;
        this.name = name;
        this.resourceType = resourceType;
    }

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
