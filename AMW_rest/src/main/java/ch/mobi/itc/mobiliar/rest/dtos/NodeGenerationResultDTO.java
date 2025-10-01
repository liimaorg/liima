package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.generator.control.ApplicationGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.business.generator.control.GenerationUnitGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.NodeGenerationResult;
import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NodeGenerationResultDTO {

    private String nodeName;
    private List<GeneratedTemplateDTO> asTemplates;
    private List<ApplicationGenerationResultDTO> appResults;
    private List<String> errors = new ArrayList<>();

    public NodeGenerationResultDTO(NodeGenerationResult nodeResult) {
        this.nodeName = nodeResult.getNode().getName();
        this.asTemplates = new ArrayList<>();
        nodeResult.getErrorMessage();
        for (TemplatePropertyException error : nodeResult.getPropertyValidationExceptions()) {
            this.errors.add(error.getMessage());
        }
        for (GenerationUnitGenerationResult generationUnitGenerationResult : nodeResult.getApplicationServerResults()) {
            for (TemplatePropertyException error : generationUnitGenerationResult.getGenerationUnitPreprocessResult().getErrorMessages()) {
                this.errors.add(error.getMessage());
            }
            for (GeneratedTemplate generatedTemplate : generationUnitGenerationResult.getGeneratedTemplates()) {
                this.asTemplates.add(new GeneratedTemplateDTO(generatedTemplate));
            }
        }
        this.appResults = new ArrayList<>();
        for (ApplicationGenerationResult applicationGenerationResult : nodeResult.getApplicationResults()) {
            this.appResults.add(new ApplicationGenerationResultDTO(applicationGenerationResult));
        }
    }
}
