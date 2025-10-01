package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.generator.control.ApplicationGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApplicationGenerationResultDTO {

    private String applicationName;
    private List<GeneratedTemplateDTO> templates;
    private List<String> errors = new ArrayList<>();

    public ApplicationGenerationResultDTO(ApplicationGenerationResult result) {
        this.applicationName = result.getApplication().getName();
        this.templates = new ArrayList<>(result.getGeneratedTemplates().size());
        for (TemplatePropertyException error : result.getPreprocessResults()) {
            this.errors.add(error.getMessage());
        }
        for (GeneratedTemplate generatedTemplate : result.getGeneratedTemplates()) {
            this.templates.add(new GeneratedTemplateDTO(generatedTemplate));
        }
    }
}

