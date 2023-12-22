package ch.puzzle.itc.mobiliar.business.deploy.boundary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentLog {

    private int deploymentId;
    private String filename;

}
