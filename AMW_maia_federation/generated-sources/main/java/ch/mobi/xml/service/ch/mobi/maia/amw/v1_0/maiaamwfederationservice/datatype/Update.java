
package ch.mobi.xml.service.ch.mobi.maia.amw.v1_0.maiaamwfederationservice.datatype;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateRequest;
import ch.mobi.xml.datatype.common.commons.v3.CallContext;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for update complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="update">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="callContext" type="{http://xml.mobi.ch/datatype/common/Commons/v3}CallContext"/>
 *         &lt;element name="fcOwner" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="update" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0}UpdateRequest"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "update", propOrder = {
    "callContext",
    "fcOwner",
    "update"
})
public class Update {

    @XmlElement(required = true)
    protected CallContext callContext;
    @XmlElement(required = true)
    protected String fcOwner;
    @XmlElement(required = true)
    protected UpdateRequest update;

    /**
     * Default no-arg constructor
     * 
     */
    public Update() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public Update(final CallContext callContext, final String fcOwner, final UpdateRequest update) {
        this.callContext = callContext;
        this.fcOwner = fcOwner;
        this.update = update;
    }

    /**
     * Gets the value of the callContext property.
     * 
     * @return
     *     possible object is
     *     {@link CallContext }
     *     
     */
    public CallContext getCallContext() {
        return callContext;
    }

    /**
     * Sets the value of the callContext property.
     * 
     * @param value
     *     allowed object is
     *     {@link CallContext }
     *     
     */
    public void setCallContext(CallContext value) {
        this.callContext = value;
    }

    /**
     * Gets the value of the fcOwner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFcOwner() {
        return fcOwner;
    }

    /**
     * Sets the value of the fcOwner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFcOwner(String value) {
        this.fcOwner = value;
    }

    /**
     * Gets the value of the update property.
     * 
     * @return
     *     possible object is
     *     {@link UpdateRequest }
     *     
     */
    public UpdateRequest getUpdate() {
        return update;
    }

    /**
     * Sets the value of the update property.
     * 
     * @param value
     *     allowed object is
     *     {@link UpdateRequest }
     *     
     */
    public void setUpdate(UpdateRequest value) {
        this.update = value;
    }

}
