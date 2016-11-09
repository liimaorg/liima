
package ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for UpdateResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpdateResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processedApplications" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0}ApplicationUpdateResult" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateResponse", propOrder = {
    "processedApplications"
})
public class UpdateResponse {

    protected List<ApplicationUpdateResult> processedApplications;

    /**
     * Default no-arg constructor
     * 
     */
    public UpdateResponse() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public UpdateResponse(final List<ApplicationUpdateResult> processedApplications) {
        this.processedApplications = processedApplications;
    }

    /**
     * Gets the value of the processedApplications property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the processedApplications property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProcessedApplications().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ApplicationUpdateResult }
     * 
     * 
     */
    public List<ApplicationUpdateResult> getProcessedApplications() {
        if (processedApplications == null) {
            processedApplications = new ArrayList<ApplicationUpdateResult>();
        }
        return this.processedApplications;
    }

}
