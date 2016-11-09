
package ch.mobi.xml.datatype.common.commons.v3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HistoryHeader complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HistoryHeader">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="validFrom" type="{http://xml.mobi.ch/datatype/common/Commons/v3}HistoryTimestamp" minOccurs="0"/>
 *         &lt;element name="validTo" type="{http://xml.mobi.ch/datatype/common/Commons/v3}HistoryTimestamp" minOccurs="0"/>
 *         &lt;element name="entryAt" type="{http://xml.mobi.ch/datatype/common/Commons/v3}HistoryTimestamp" minOccurs="0"/>
 *         &lt;element name="userId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="replacedAt" type="{http://xml.mobi.ch/datatype/common/Commons/v3}HistoryTimestamp" minOccurs="0"/>
 *         &lt;element name="state" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HistoryHeader", propOrder = {
    "validFrom",
    "validTo",
    "entryAt",
    "userId",
    "replacedAt",
    "state"
})
public class HistoryHeader {

    protected String validFrom;
    protected String validTo;
    protected String entryAt;
    protected String userId;
    protected String replacedAt;
    protected Integer state;

    /**
     * Default no-arg constructor
     * 
     */
    public HistoryHeader() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public HistoryHeader(final String validFrom, final String validTo, final String entryAt, final String userId, final String replacedAt, final Integer state) {
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.entryAt = entryAt;
        this.userId = userId;
        this.replacedAt = replacedAt;
        this.state = state;
    }

    /**
     * Gets the value of the validFrom property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValidFrom() {
        return validFrom;
    }

    /**
     * Sets the value of the validFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidFrom(String value) {
        this.validFrom = value;
    }

    /**
     * Gets the value of the validTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValidTo() {
        return validTo;
    }

    /**
     * Sets the value of the validTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidTo(String value) {
        this.validTo = value;
    }

    /**
     * Gets the value of the entryAt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEntryAt() {
        return entryAt;
    }

    /**
     * Sets the value of the entryAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEntryAt(String value) {
        this.entryAt = value;
    }

    /**
     * Gets the value of the userId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserId(String value) {
        this.userId = value;
    }

    /**
     * Gets the value of the replacedAt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplacedAt() {
        return replacedAt;
    }

    /**
     * Sets the value of the replacedAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplacedAt(String value) {
        this.replacedAt = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setState(Integer value) {
        this.state = value;
    }

}
