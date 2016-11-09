
package ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ApplicationPayload complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ApplicationPayload">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="providedPorts" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}ProvidedPort" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="consumedPorts" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}ConsumedPort" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="properties" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}PropertyDeclaration" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ApplicationPayload", propOrder = {
    "providedPorts",
    "consumedPorts",
    "properties"
})
public class ApplicationPayload {

    protected List<ProvidedPort> providedPorts;
    protected List<ConsumedPort> consumedPorts;
    protected List<PropertyDeclaration> properties;

    /**
     * Default no-arg constructor
     * 
     */
    public ApplicationPayload() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public ApplicationPayload(final List<ProvidedPort> providedPorts, final List<ConsumedPort> consumedPorts, final List<PropertyDeclaration> properties) {
        this.providedPorts = providedPorts;
        this.consumedPorts = consumedPorts;
        this.properties = properties;
    }

    /**
     * Gets the value of the providedPorts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the providedPorts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProvidedPorts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProvidedPort }
     * 
     * 
     */
    public List<ProvidedPort> getProvidedPorts() {
        if (providedPorts == null) {
            providedPorts = new ArrayList<ProvidedPort>();
        }
        return this.providedPorts;
    }

    /**
     * Gets the value of the consumedPorts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the consumedPorts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConsumedPorts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConsumedPort }
     * 
     * 
     */
    public List<ConsumedPort> getConsumedPorts() {
        if (consumedPorts == null) {
            consumedPorts = new ArrayList<ConsumedPort>();
        }
        return this.consumedPorts;
    }

    /**
     * Gets the value of the properties property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the properties property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperties().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyDeclaration }
     * 
     * 
     */
    public List<PropertyDeclaration> getProperties() {
        if (properties == null) {
            properties = new ArrayList<PropertyDeclaration>();
        }
        return this.properties;
    }

}
