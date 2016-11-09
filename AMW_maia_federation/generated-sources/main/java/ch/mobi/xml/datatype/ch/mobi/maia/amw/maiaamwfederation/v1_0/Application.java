
package ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for Application complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Application">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}ApplicationID"/>
 *         &lt;element name="techStack" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="fcKey" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="fcLink" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="outOfServiceByRelease" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="structure" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}ApplicationReleaseBinding" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Application", propOrder = {
    "id",
    "techStack",
    "fcKey",
    "fcLink",
    "outOfServiceByRelease",
    "structure"
})
public class Application {

    @XmlElement(required = true)
    protected ApplicationID id;
    @XmlElement(required = true)
    protected String techStack;
    @XmlElement(required = true)
    protected String fcKey;
    protected String fcLink;
    protected String outOfServiceByRelease;
    protected List<ApplicationReleaseBinding> structure;

    /**
     * Default no-arg constructor
     * 
     */
    public Application() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public Application(final ApplicationID id, final String techStack, final String fcKey, final String fcLink, final String outOfServiceByRelease, final List<ApplicationReleaseBinding> structure) {
        this.id = id;
        this.techStack = techStack;
        this.fcKey = fcKey;
        this.fcLink = fcLink;
        this.outOfServiceByRelease = outOfServiceByRelease;
        this.structure = structure;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationID }
     *     
     */
    public ApplicationID getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationID }
     *     
     */
    public void setId(ApplicationID value) {
        this.id = value;
    }

    /**
     * Gets the value of the techStack property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTechStack() {
        return techStack;
    }

    /**
     * Sets the value of the techStack property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTechStack(String value) {
        this.techStack = value;
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
     * Gets the value of the outOfServiceByRelease property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutOfServiceByRelease() {
        return outOfServiceByRelease;
    }

    /**
     * Sets the value of the outOfServiceByRelease property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutOfServiceByRelease(String value) {
        this.outOfServiceByRelease = value;
    }

    /**
     * Gets the value of the structure property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the structure property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStructure().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ApplicationReleaseBinding }
     * 
     * 
     */
    public List<ApplicationReleaseBinding> getStructure() {
        if (structure == null) {
            structure = new ArrayList<ApplicationReleaseBinding>();
        }
        return this.structure;
    }

}
