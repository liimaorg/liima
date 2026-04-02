package ch.puzzle.itc.mobiliar.business.property.command;

import java.util.List;

public interface PropertyDescriptorData {
    String getName();
    String getDisplayName();
    String getValidationRegex();
    boolean isNullable();
    boolean isOptional();
    boolean isEncrypted();
    String getMik();
    String getDefaultValue();
    String getExampleValue();
    String getComment();
    List<String> getPropertyTags();
}
