package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "copyFromCandidate")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CopyFromCandidateDTO {
    private Integer groupId;
    private String groupName;
    private List<CopyFromReleaseDTO> releases;

    public static List<CopyFromCandidateDTO> from(ResourceEntity resource, List<ResourceGroup> groups) {

        List<CopyFromCandidateDTO> candidates = new ArrayList<>();
        for (ResourceGroup group : groups) {
            LinkedHashMap<String, Integer> releaseMap = group.getReleaseToResourceMap();
            if (releaseMap.isEmpty()) {
                continue;
            }
            // exclude the target resource's own group if only its own release remains
            if (group.getId().equals(resource.getResourceGroup().getId()) && releaseMap.isEmpty()) {
                continue;
            }
            List<CopyFromReleaseDTO> releases = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : releaseMap.entrySet()) {
                releases.add(new CopyFromReleaseDTO(null, entry.getKey(), entry.getValue()));
            }
            candidates.add(new CopyFromCandidateDTO(group.getId(), group.getName(), releases));
        }
        return candidates;
    }
}
