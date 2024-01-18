package ch.puzzle.itc.mobiliar.business.deploy.boundary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DeploymentLogContent {

    private long id;
    private String fileName;
    private String content;

}
