package ch.mobi.itc.mobiliar.rest.tags.usecases;

import ch.mobi.itc.mobiliar.rest.dtos.TagDTO;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;

@Getter
public class TagCommand {

    @NotNull
    private String name;

    public TagCommand(TagDTO tagDTO) throws ValidationException {
        this.requireNonNull(tagDTO);
        this.requireNotEmpty(tagDTO.getName());
        this.name = tagDTO.getName();
    }

    private void requireNotEmpty(String string) throws ValidationException {
        if (StringUtils.isBlank(string)) throw new ValidationException("Tag name must not be null or empty.");
    }

    private void requireNonNull(Object obj) throws ValidationException {
        if (obj == null) throw new ValidationException("Tag must not be null.");
    }
}
