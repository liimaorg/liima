
package ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.Application;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.ApplicationID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for UpdateRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpdateRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="applications" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}Application" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="applicationPredecessors" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0}ApplicationPredecessorRelation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="removedApplications" type="{http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0}ApplicationID" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateRequest", propOrder = {
    "applications",
    "applicationPredecessors",
    "removedApplications"
})
public class UpdateRequest {

    protected List<Application> applications;
    protected List<ApplicationPredecessorRelation> applicationPredecessors;
    protected List<ApplicationID> removedApplications;

    /**
     * Default no-arg constructor
     * 
     */
    public UpdateRequest() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public UpdateRequest(final List<Application> applications, final List<ApplicationPredecessorRelation> applicationPredecessors, final List<ApplicationID> removedApplications) {
        this.applications = applications;
        this.applicationPredecessors = applicationPredecessors;
        this.removedApplications = removedApplications;
    }

    /**
     * Gets the value of the applications property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the applications property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApplications().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Application }
     * 
     * 
     */
    public List<Application> getApplications() {
        if (applications == null) {
            applications = new ArrayList<Application>();
        }
        return this.applications;
    }

    /**
     * Gets the value of the applicationPredecessors property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the applicationPredecessors property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApplicationPredecessors().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ApplicationPredecessorRelation }
     * 
     * 
     */
    public List<ApplicationPredecessorRelation> getApplicationPredecessors() {
        if (applicationPredecessors == null) {
            applicationPredecessors = new ArrayList<ApplicationPredecessorRelation>();
        }
        return this.applicationPredecessors;
    }

    /**
     * Gets the value of the removedApplications property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the removedApplications property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRemovedApplications().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ApplicationID }
     * 
     * 
     */
    public List<ApplicationID> getRemovedApplications() {
        if (removedApplications == null) {
            removedApplications = new ArrayList<ApplicationID>();
        }
        return this.removedApplications;
    }

}
