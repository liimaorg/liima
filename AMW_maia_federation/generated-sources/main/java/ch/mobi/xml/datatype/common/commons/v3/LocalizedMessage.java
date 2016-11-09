
package ch.mobi.xml.datatype.common.commons.v3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for LocalizedMessage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LocalizedMessage">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="message" type="{http://xml.mobi.ch/datatype/common/Commons/v3}Message"/>
 *         &lt;element name="translation" type="{http://xml.mobi.ch/datatype/common/Commons/v3}LocalizedString" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LocalizedMessage", propOrder = {
    "message",
    "translation"
})
public class LocalizedMessage {

    @XmlElement(required = true)
    protected Message message;
    protected List<LocalizedString> translation;

    /**
     * Default no-arg constructor
     * 
     */
    public LocalizedMessage() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public LocalizedMessage(final Message message, final List<LocalizedString> translation) {
        this.message = message;
        this.translation = translation;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link Message }
     *     
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link Message }
     *     
     */
    public void setMessage(Message value) {
        this.message = value;
    }

    /**
     * Gets the value of the translation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the translation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTranslation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LocalizedString }
     * 
     * 
     */
    public List<LocalizedString> getTranslation() {
        if (translation == null) {
            translation = new ArrayList<LocalizedString>();
        }
        return this.translation;
    }

}
