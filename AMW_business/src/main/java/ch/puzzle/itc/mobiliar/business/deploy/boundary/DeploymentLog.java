package ch.puzzle.itc.mobiliar.business.deploy.boundary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentLog {

    private long id;
    private String filename;
    private String content;

    public DeploymentLog(Integer id, String filename) {
        this(id, filename, null);
    }
}
