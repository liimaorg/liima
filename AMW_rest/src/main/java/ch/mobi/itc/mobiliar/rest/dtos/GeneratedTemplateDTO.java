package ch.mobi.itc.mobiliar.rest.dtos;

import java.util.ArrayList;
import java.util.List;

import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException;
import lombok.Data;

@Data
public class GeneratedTemplateDTO {

    private String name;
    private String path;
    private String content;
    private List<String> errors = new ArrayList<>();

    public GeneratedTemplateDTO(GeneratedTemplate template) {
        this.name = template.getName();
        this.path = template.getPath();
        this.content = template.getContent();
        for (TemplatePropertyException exception : template.getErrorMessages()) {
            this.errors.add(exception.getMessage());
        }
    }
}
