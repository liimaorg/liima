
package ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.ApplicationID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ApplicationPredecessorRelation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ApplicationPredecessorRelation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="newApplication" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}ApplicationID"/>
 *         &lt;element name="predecessorApplication" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}ApplicationID"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ApplicationPredecessorRelation", propOrder = {
    "newApplication",
    "predecessorApplication"
})
public class ApplicationPredecessorRelation {

    @XmlElement(required = true)
    protected ApplicationID newApplication;
    @XmlElement(required = true)
    protected ApplicationID predecessorApplication;

    /**
     * Default no-arg constructor
     * 
     */
    public ApplicationPredecessorRelation() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public ApplicationPredecessorRelation(final ApplicationID newApplication, final ApplicationID predecessorApplication) {
        this.newApplication = newApplication;
        this.predecessorApplication = predecessorApplication;
    }

    /**
     * Gets the value of the newApplication property.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationID }
     *     
     */
    public ApplicationID getNewApplication() {
        return newApplication;
    }

    /**
     * Sets the value of the newApplication property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationID }
     *     
     */
    public void setNewApplication(ApplicationID value) {
        this.newApplication = value;
    }

    /**
     * Gets the value of the predecessorApplication property.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationID }
     *     
     */
    public ApplicationID getPredecessorApplication() {
        return predecessorApplication;
    }

    /**
     * Sets the value of the predecessorApplication property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationID }
     *     
     */
    public void setPredecessorApplication(ApplicationID value) {
        this.predecessorApplication = value;
    }

}
