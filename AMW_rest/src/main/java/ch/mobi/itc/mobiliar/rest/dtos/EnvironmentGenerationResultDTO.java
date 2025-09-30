package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.generator.control.EnvironmentGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.NodeGenerationResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EnvironmentGenerationResultDTO {

    private String releaseName;
    private String applicationServerName;
    private List<NodeGenerationResultDTO> nodeGenerationResults;
    private String error;

    public EnvironmentGenerationResultDTO(EnvironmentGenerationResult generationResult) {
        this.releaseName = generationResult.getGenerationContext().getApplicationServer().getRelease().getName();
        this.applicationServerName = generationResult.getGenerationContext().getApplicationServer().getName();
        if(generationResult.getEnvironmentException() != null){
            error = generationResult.getEnvironmentException().getMessage();
		}
        this.nodeGenerationResults = new ArrayList<>(generationResult.getNodeGenerationResults().size());
        for (NodeGenerationResult nodeGenerationResult : generationResult.getNodeGenerationResults()) {
            this.nodeGenerationResults.add(new NodeGenerationResultDTO(nodeGenerationResult));
        }
    }
}
