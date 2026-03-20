package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "propertyDescriptor")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDescriptorDTO {

    private Integer id;
    
    private Long version;
    
    @NotNull(message = "Property name must not be null.")
    @NotEmpty(message = "Property name must not be empty.")
    private String name;
    
    private String displayName;
    
    private String validationRegex;
    
    private boolean nullable;
    
    private boolean optional;
    
    private boolean encrypted;
    
    private String mik;
    
    private String defaultValue;
    
    private String exampleValue;
    
    private String comment;
    
    private PropertyTypeDTO propertyTypeEntity;
    
    private List<PropertyTagDTO> propertyTags;

    public PropertyDescriptorDTO(PropertyDescriptorEntity entity) {
        this.id = entity.getId();
        this.version = entity.getV();
        this.name = entity.getPropertyName();
        this.displayName = entity.getDisplayName();
        this.validationRegex = entity.getValidationLogic();
        this.nullable = entity.isNullable();
        this.optional = entity.isOptional();
        this.encrypted = entity.isEncrypt();
        this.mik = entity.getMachineInterpretationKey();
        this.defaultValue = entity.getDefaultValue();
        this.exampleValue = entity.getExampleValue();
        this.comment = entity.getPropertyComment();
        
        if (entity.getPropertyTypeEntity() != null) {
            // Create a simplified PropertyTypeDTO without lazy-loaded tags
            PropertyTypeDTO typeDTO = new PropertyTypeDTO();
            typeDTO.setId(entity.getPropertyTypeEntity().getId());
            typeDTO.setName(entity.getPropertyTypeEntity().getPropertyTypeName());
            typeDTO.setEncrypted(entity.getPropertyTypeEntity().isEncrypt());
            typeDTO.setValidationRegex(entity.getPropertyTypeEntity().getValidationRegex());
            typeDTO.setPropertyTags(new ArrayList<>());
            this.propertyTypeEntity = typeDTO;
        }
        
        this.propertyTags = new ArrayList<>();
        for (PropertyTagEntity tag : entity.getPropertyTags()) {
            this.propertyTags.add(new PropertyTagDTO(tag));
        }
    }

    public PropertyDescriptorEntity toEntity() {
        PropertyDescriptorEntity entity = new PropertyDescriptorEntity();
        entity.setId(this.id);
        entity.setPropertyName(this.name);
        entity.setDisplayName(this.displayName);
        entity.setValidationLogic(this.validationRegex);
        entity.setNullable(this.nullable);
        entity.setOptional(this.optional);
        entity.setEncrypt(this.encrypted);
        entity.setMachineInterpretationKey(this.mik);
        entity.setDefaultValue(this.defaultValue);
        entity.setExampleValue(this.exampleValue);
        entity.setPropertyComment(this.comment);
        return entity;
    }
}
