package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;

@Getter
public class TagCommand {

    @NotNull
    private String name;

    public TagCommand(String tagName) throws ValidationException {
        this.requireNotEmpty(tagName);
        this.name = tagName;
    }

    private void requireNotEmpty(String string) throws ValidationException {
        if (StringUtils.isBlank(string)) throw new ValidationException("Tag name must not be null or empty.");
    }
}
