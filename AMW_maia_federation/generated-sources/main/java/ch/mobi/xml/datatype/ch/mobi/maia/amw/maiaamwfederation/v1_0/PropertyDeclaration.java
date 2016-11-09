
package ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for PropertyDeclaration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PropertyDeclaration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="technicalKey" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tags" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="defaultValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="exampleValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="encrypted" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="machineInterpretationKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="validationPattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isKeyOptional" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="isValueOptional" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PropertyDeclaration", propOrder = {
    "technicalKey",
    "displayName",
    "tags",
    "defaultValue",
    "exampleValue",
    "encrypted",
    "machineInterpretationKey",
    "validationPattern",
    "isKeyOptional",
    "isValueOptional"
})
public class PropertyDeclaration {

    @XmlElement(required = true)
    protected String technicalKey;
    protected String displayName;
    protected List<String> tags;
    protected String defaultValue;
    protected String exampleValue;
    protected boolean encrypted;
    protected String machineInterpretationKey;
    protected String validationPattern;
    protected boolean isKeyOptional;
    protected boolean isValueOptional;

    /**
     * Default no-arg constructor
     * 
     */
    public PropertyDeclaration() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public PropertyDeclaration(final String technicalKey, final String displayName, final List<String> tags, final String defaultValue, final String exampleValue, final boolean encrypted, final String machineInterpretationKey, final String validationPattern, final boolean isKeyOptional, final boolean isValueOptional) {
        this.technicalKey = technicalKey;
        this.displayName = displayName;
        this.tags = tags;
        this.defaultValue = defaultValue;
        this.exampleValue = exampleValue;
        this.encrypted = encrypted;
        this.machineInterpretationKey = machineInterpretationKey;
        this.validationPattern = validationPattern;
        this.isKeyOptional = isKeyOptional;
        this.isValueOptional = isValueOptional;
    }

    /**
     * Gets the value of the technicalKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTechnicalKey() {
        return technicalKey;
    }

    /**
     * Sets the value of the technicalKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTechnicalKey(String value) {
        this.technicalKey = value;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the tags property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tags property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTags().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getTags() {
        if (tags == null) {
            tags = new ArrayList<String>();
        }
        return this.tags;
    }

    /**
     * Gets the value of the defaultValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the value of the defaultValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    /**
     * Gets the value of the exampleValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExampleValue() {
        return exampleValue;
    }

    /**
     * Sets the value of the exampleValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExampleValue(String value) {
        this.exampleValue = value;
    }

    /**
     * Gets the value of the encrypted property.
     * 
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Sets the value of the encrypted property.
     * 
     */
    public void setEncrypted(boolean value) {
        this.encrypted = value;
    }

    /**
     * Gets the value of the machineInterpretationKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMachineInterpretationKey() {
        return machineInterpretationKey;
    }

    /**
     * Sets the value of the machineInterpretationKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMachineInterpretationKey(String value) {
        this.machineInterpretationKey = value;
    }

    /**
     * Gets the value of the validationPattern property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValidationPattern() {
        return validationPattern;
    }

    /**
     * Sets the value of the validationPattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidationPattern(String value) {
        this.validationPattern = value;
    }

    /**
     * Gets the value of the isKeyOptional property.
     * 
     */
    public boolean isIsKeyOptional() {
        return isKeyOptional;
    }

    /**
     * Sets the value of the isKeyOptional property.
     * 
     */
    public void setIsKeyOptional(boolean value) {
        this.isKeyOptional = value;
    }

    /**
     * Gets the value of the isValueOptional property.
     * 
     */
    public boolean isIsValueOptional() {
        return isValueOptional;
    }

    /**
     * Sets the value of the isValueOptional property.
     * 
     */
    public void setIsValueOptional(boolean value) {
        this.isValueOptional = value;
    }

}
