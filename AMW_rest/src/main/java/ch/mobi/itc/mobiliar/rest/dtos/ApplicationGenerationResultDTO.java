package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.generator.control.ApplicationGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApplicationGenerationResultDTO {

    private String applicationName;
    private List<GeneratedTemplateDTO> templates;

    public ApplicationGenerationResultDTO(ApplicationGenerationResult result) {
        this.applicationName = result.getApplication().getName();
        this.templates = new ArrayList<>(result.getGeneratedTemplates().size());
        for (GeneratedTemplate generatedTemplate : result.getGeneratedTemplates()) {
            this.templates.add(new GeneratedTemplateDTO(generatedTemplate));
        }
    }
}

