
package ch.mobi.xml.datatype.common.commons.v3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


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
 *         &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="severity" type="{http://xml.mobi.ch/datatype/common/Commons/v3}MessageSeverity"/>
 *         &lt;element name="logMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="messageParameter" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
    "code",
    "severity",
    "logMessage",
    "messageParameter"
})
public class Message {

    @XmlElement(required = true)
    protected String code;
    @XmlElement(required = true)
    protected MessageSeverity severity;
    @XmlElement(required = true)
    protected String logMessage;
    protected List<String> messageParameter;

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
    public Message(final String code, final MessageSeverity severity, final String logMessage, final List<String> messageParameter) {
        this.code = code;
        this.severity = severity;
        this.logMessage = logMessage;
        this.messageParameter = messageParameter;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
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
     * Gets the value of the logMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogMessage() {
        return logMessage;
    }

    /**
     * Sets the value of the logMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogMessage(String value) {
        this.logMessage = value;
    }

    /**
     * Gets the value of the messageParameter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageParameter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageParameter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMessageParameter() {
        if (messageParameter == null) {
            messageParameter = new ArrayList<String>();
        }
        return this.messageParameter;
    }

}
