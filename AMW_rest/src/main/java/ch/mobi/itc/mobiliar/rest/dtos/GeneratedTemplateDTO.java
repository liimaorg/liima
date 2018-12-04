package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import lombok.Data;

@Data
public class GeneratedTemplateDTO {

    private String name;
    private String path;
    private String content;

    public GeneratedTemplateDTO(GeneratedTemplate template) {
        this.name = template.getName();
        this.path = template.getPath();
        this.content = template.getContent();
    }
}
