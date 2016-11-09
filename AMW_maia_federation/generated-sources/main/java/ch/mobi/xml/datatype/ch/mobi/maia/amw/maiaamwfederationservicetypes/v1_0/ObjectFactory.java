
package ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Message_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0", "Message");
    private final static QName _UpdateRequest_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0", "UpdateRequest");
    private final static QName _ApplicationPredecessorRelation_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0", "ApplicationPredecessorRelation");
    private final static QName _ApplicationUpdateResult_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0", "ApplicationUpdateResult");
    private final static QName _UpdateResponse_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0", "UpdateResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ApplicationPredecessorRelation }
     * 
     */
    public ApplicationPredecessorRelation createApplicationPredecessorRelation() {
        return new ApplicationPredecessorRelation();
    }

    /**
     * Create an instance of {@link UpdateRequest }
     * 
     */
    public UpdateRequest createUpdateRequest() {
        return new UpdateRequest();
    }

    /**
     * Create an instance of {@link Message }
     * 
     */
    public Message createMessage() {
        return new Message();
    }

    /**
     * Create an instance of {@link ApplicationUpdateResult }
     * 
     */
    public ApplicationUpdateResult createApplicationUpdateResult() {
        return new ApplicationUpdateResult();
    }

    /**
     * Create an instance of {@link UpdateResponse }
     * 
     */
    public UpdateResponse createUpdateResponse() {
        return new UpdateResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Message }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0", name = "Message")
    public JAXBElement<Message> createMessage(Message value) {
        return new JAXBElement<Message>(_Message_QNAME, Message.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0", name = "UpdateRequest")
    public JAXBElement<UpdateRequest> createUpdateRequest(UpdateRequest value) {
        return new JAXBElement<UpdateRequest>(_UpdateRequest_QNAME, UpdateRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ApplicationPredecessorRelation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0", name = "ApplicationPredecessorRelation")
    public JAXBElement<ApplicationPredecessorRelation> createApplicationPredecessorRelation(ApplicationPredecessorRelation value) {
        return new JAXBElement<ApplicationPredecessorRelation>(_ApplicationPredecessorRelation_QNAME, ApplicationPredecessorRelation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ApplicationUpdateResult }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0", name = "ApplicationUpdateResult")
    public JAXBElement<ApplicationUpdateResult> createApplicationUpdateResult(ApplicationUpdateResult value) {
        return new JAXBElement<ApplicationUpdateResult>(_ApplicationUpdateResult_QNAME, ApplicationUpdateResult.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederationServiceTypes/v1_0", name = "UpdateResponse")
    public JAXBElement<UpdateResponse> createUpdateResponse(UpdateResponse value) {
        return new JAXBElement<UpdateResponse>(_UpdateResponse_QNAME, UpdateResponse.class, null, value);
    }

}
