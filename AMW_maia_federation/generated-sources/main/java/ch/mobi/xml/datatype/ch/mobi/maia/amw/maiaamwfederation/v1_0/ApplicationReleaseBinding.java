
package ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ApplicationReleaseBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ApplicationReleaseBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="release" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="payload" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}ApplicationPayload"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ApplicationReleaseBinding", propOrder = {
    "release",
    "payload"
})
public class ApplicationReleaseBinding {

    @XmlElement(required = true)
    protected String release;
    @XmlElement(required = true)
    protected ApplicationPayload payload;

    /**
     * Default no-arg constructor
     * 
     */
    public ApplicationReleaseBinding() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public ApplicationReleaseBinding(final String release, final ApplicationPayload payload) {
        this.release = release;
        this.payload = payload;
    }

    /**
     * Gets the value of the release property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelease() {
        return release;
    }

    /**
     * Sets the value of the release property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelease(String value) {
        this.release = value;
    }

    /**
     * Gets the value of the payload property.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationPayload }
     *     
     */
    public ApplicationPayload getPayload() {
        return payload;
    }

    /**
     * Sets the value of the payload property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationPayload }
     *     
     */
    public void setPayload(ApplicationPayload value) {
        this.payload = value;
    }

}
