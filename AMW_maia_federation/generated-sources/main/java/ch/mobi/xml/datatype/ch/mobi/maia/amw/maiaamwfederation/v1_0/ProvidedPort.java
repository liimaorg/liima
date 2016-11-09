
package ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ProvidedPort complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProvidedPort">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}ProvidedPortID"/>
 *         &lt;element name="fcKey" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="fcLink" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="localPortID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="resourceType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="properties" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}PropertyDeclaration" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProvidedPort", propOrder = {
    "id",
    "fcKey",
    "fcLink",
    "displayName",
    "localPortID",
    "resourceType",
    "properties"
})
public class ProvidedPort {

    @XmlElement(required = true)
    protected ProvidedPortID id;
    @XmlElement(required = true)
    protected String fcKey;
    @XmlElement(required = true)
    protected String fcLink;
    @XmlElement(required = true)
    protected String displayName;
    @XmlElement(required = true)
    protected String localPortID;
    @XmlElement(required = true)
    protected String resourceType;
    protected List<PropertyDeclaration> properties;

    /**
     * Default no-arg constructor
     * 
     */
    public ProvidedPort() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public ProvidedPort(final ProvidedPortID id, final String fcKey, final String fcLink, final String displayName, final String localPortID, final String resourceType, final List<PropertyDeclaration> properties) {
        this.id = id;
        this.fcKey = fcKey;
        this.fcLink = fcLink;
        this.displayName = displayName;
        this.localPortID = localPortID;
        this.resourceType = resourceType;
        this.properties = properties;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link ProvidedPortID }
     *     
     */
    public ProvidedPortID getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProvidedPortID }
     *     
     */
    public void setId(ProvidedPortID value) {
        this.id = value;
    }

    /**
     * Gets the value of the fcKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFcKey() {
        return fcKey;
    }

    /**
     * Sets the value of the fcKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFcKey(String value) {
        this.fcKey = value;
    }

    /**
     * Gets the value of the fcLink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFcLink() {
        return fcLink;
    }

    /**
     * Sets the value of the fcLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFcLink(String value) {
        this.fcLink = value;
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
     * Gets the value of the localPortID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalPortID() {
        return localPortID;
    }

    /**
     * Sets the value of the localPortID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalPortID(String value) {
        this.localPortID = value;
    }

    /**
     * Gets the value of the resourceType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Sets the value of the resourceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResourceType(String value) {
        this.resourceType = value;
    }

    /**
     * Gets the value of the properties property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the properties property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperties().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyDeclaration }
     * 
     * 
     */
    public List<PropertyDeclaration> getProperties() {
        if (properties == null) {
            properties = new ArrayList<PropertyDeclaration>();
        }
        return this.properties;
    }

}
