
package ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.ApplicationID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ApplicationUpdateResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ApplicationUpdateResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}ApplicationID"/>
 *         &lt;element name="state" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0}ProcessingState"/>
 *         &lt;element name="amwLink" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="messages" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0}Message" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ApplicationUpdateResult", propOrder = {
    "id",
    "state",
    "amwLink",
    "messages"
})
public class ApplicationUpdateResult {

    @XmlElement(required = true)
    protected ApplicationID id;
    @XmlElement(required = true)
    protected ProcessingState state;
    protected String amwLink;
    protected List<Message> messages;

    /**
     * Default no-arg constructor
     * 
     */
    public ApplicationUpdateResult() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public ApplicationUpdateResult(final ApplicationID id, final ProcessingState state, final String amwLink, final List<Message> messages) {
        this.id = id;
        this.state = state;
        this.amwLink = amwLink;
        this.messages = messages;
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
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessingState }
     *     
     */
    public ProcessingState getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessingState }
     *     
     */
    public void setState(ProcessingState value) {
        this.state = value;
    }

    /**
     * Gets the value of the amwLink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAmwLink() {
        return amwLink;
    }

    /**
     * Sets the value of the amwLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAmwLink(String value) {
        this.amwLink = value;
    }

    /**
     * Gets the value of the messages property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messages property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessages().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Message }
     * 
     * 
     */
    public List<Message> getMessages() {
        if (messages == null) {
            messages = new ArrayList<Message>();
        }
        return this.messages;
    }

}
