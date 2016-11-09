
package ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0;

import ch.mobi.xml.datatype.common.commons.v3.MessageSeverity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Message complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Message">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="severity" type="{http://xml.mobi.ch/datatype/common/Commons/v3}MessageSeverity"/>
 *         &lt;element name="humanReadableMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Message", propOrder = {
    "severity",
    "humanReadableMessage"
})
public class Message {

    @XmlElement(required = true)
    protected MessageSeverity severity;
    @XmlElement(required = true)
    protected String humanReadableMessage;

    /**
     * Default no-arg constructor
     * 
     */
    public Message() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public Message(final MessageSeverity severity, final String humanReadableMessage) {
        this.severity = severity;
        this.humanReadableMessage = humanReadableMessage;
    }

    /**
     * Gets the value of the severity property.
     * 
     * @return
     *     possible object is
     *     {@link MessageSeverity }
     *     
     */
    public MessageSeverity getSeverity() {
        return severity;
    }

    /**
     * Sets the value of the severity property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageSeverity }
     *     
     */
    public void setSeverity(MessageSeverity value) {
        this.severity = value;
    }

    /**
     * Gets the value of the humanReadableMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHumanReadableMessage() {
        return humanReadableMessage;
    }

    /**
     * Sets the value of the humanReadableMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHumanReadableMessage(String value) {
        this.humanReadableMessage = value;
    }

}
