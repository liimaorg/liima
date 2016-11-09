
package ch.mobi.xml.datatype.common.commons.v3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ISDVInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ISDVInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="codeType" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="stringCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isdvText" type="{http://xml.mobi.ch/datatype/common/Commons/v3}LocalizedString" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ISDVInfo", propOrder = {
    "code",
    "codeType",
    "stringCode",
    "isdvText"
})
public class ISDVInfo {

    protected Integer code;
    protected Integer codeType;
    protected String stringCode;
    protected List<LocalizedString> isdvText;

    /**
     * Default no-arg constructor
     * 
     */
    public ISDVInfo() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public ISDVInfo(final Integer code, final Integer codeType, final String stringCode, final List<LocalizedString> isdvText) {
        this.code = code;
        this.codeType = codeType;
        this.stringCode = stringCode;
        this.isdvText = isdvText;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCode(Integer value) {
        this.code = value;
    }

    /**
     * Gets the value of the codeType property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCodeType() {
        return codeType;
    }

    /**
     * Sets the value of the codeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCodeType(Integer value) {
        this.codeType = value;
    }

    /**
     * Gets the value of the stringCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStringCode() {
        return stringCode;
    }

    /**
     * Sets the value of the stringCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStringCode(String value) {
        this.stringCode = value;
    }

    /**
     * Gets the value of the isdvText property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the isdvText property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIsdvText().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LocalizedString }
     * 
     * 
     */
    public List<LocalizedString> getIsdvText() {
        if (isdvText == null) {
            isdvText = new ArrayList<LocalizedString>();
        }
        return this.isdvText;
    }

}
