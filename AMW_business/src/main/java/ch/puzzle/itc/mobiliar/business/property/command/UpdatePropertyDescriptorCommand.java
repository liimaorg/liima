package ch.puzzle.itc.mobiliar.business.property.command;

import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class UpdatePropertyDescriptorCommand implements PropertyDescriptorCommandData {

    @NotNull(message = "Property descriptor ID may not be null")
    private final Integer descriptorId;

    private final Integer resourceId;
    private final Integer resourceTypeId;

    @NotNull(message = "Property name must not be null")
    @NotEmpty(message = "Property name must not be empty")
    private final String name;
    
    private final String displayName;
    private final String validationRegex;
    private final boolean nullable;
    private final boolean optional;
    private final boolean encrypted;
    private final String mik;
    private final String defaultValue;
    private final String exampleValue;
    private final String comment;
    private final List<String> propertyTags;

    public UpdatePropertyDescriptorCommand(Integer descriptorId, Integer resourceId, Integer resourceTypeId, PropertyDescriptorData data) throws IllegalArgumentException {
        if (resourceId == null && resourceTypeId == null) {
            throw new IllegalArgumentException("Either resourceId or resourceTypeId must be provided");
        }
        if (resourceId != null && resourceTypeId != null) {
            throw new IllegalArgumentException("Only one of resourceId or resourceTypeId should be provided");
        }
        this.descriptorId = descriptorId;
        this.resourceId = resourceId;
        this.resourceTypeId = resourceTypeId;
        this.name = data.getName();
        this.displayName = data.getDisplayName();
        this.validationRegex = data.getValidationRegex();
        this.nullable = data.isNullable();
        this.optional = data.isOptional();
        this.encrypted = data.isEncrypted();
        this.mik = data.getMik();
        this.defaultValue = data.getDefaultValue();
        this.exampleValue = data.getExampleValue();
        this.comment = data.getComment();
        this.propertyTags = data.getPropertyTags();
        validate(this);
    }

    public boolean isForResource() {
        return resourceId != null;
    }
}
