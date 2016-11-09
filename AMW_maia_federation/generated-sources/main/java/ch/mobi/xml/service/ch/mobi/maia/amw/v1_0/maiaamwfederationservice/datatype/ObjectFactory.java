
package ch.mobi.xml.service.ch.mobi.maia.amw.v1_0.maiaamwfederationservice.datatype;

import ch.mobi.xml.datatype.common.commons.v3.BusinessException;
import ch.mobi.xml.datatype.common.commons.v3.TechnicalException;
import ch.mobi.xml.datatype.common.commons.v3.ValidationException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ch.mobi.xml.service.ch.mobi.maia.amw.v1_0.maiaamwfederationservice.datatype package. 
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

    private final static QName _PingResponse_QNAME = new QName("http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", "pingResponse");
    private final static QName _ValidationException_QNAME = new QName("http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", "ValidationException");
    private final static QName _TechnicalException_QNAME = new QName("http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", "TechnicalException");
    private final static QName _BusinessException_QNAME = new QName("http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", "BusinessException");
    private final static QName _Update_QNAME = new QName("http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", "update");
    private final static QName _Ping_QNAME = new QName("http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", "ping");
    private final static QName _UpdateResponse_QNAME = new QName("http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", "updateResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ch.mobi.xml.service.ch.mobi.maia.amw.v1_0.maiaamwfederationservice.datatype
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Update }
     * 
     */
    public Update createUpdate() {
        return new Update();
    }

    /**
     * Create an instance of {@link UpdateResponse }
     * 
     */
    public UpdateResponse createUpdateResponse() {
        return new UpdateResponse();
    }

    /**
     * Create an instance of {@link Ping }
     * 
     */
    public Ping createPing() {
        return new Ping();
    }

    /**
     * Create an instance of {@link PingResponse }
     * 
     */
    public PingResponse createPingResponse() {
        return new PingResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PingResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", name = "pingResponse")
    public JAXBElement<PingResponse> createPingResponse(PingResponse value) {
        return new JAXBElement<PingResponse>(_PingResponse_QNAME, PingResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidationException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", name = "ValidationException")
    public JAXBElement<ValidationException> createValidationException(ValidationException value) {
        return new JAXBElement<ValidationException>(_ValidationException_QNAME, ValidationException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TechnicalException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", name = "TechnicalException")
    public JAXBElement<TechnicalException> createTechnicalException(TechnicalException value) {
        return new JAXBElement<TechnicalException>(_TechnicalException_QNAME, TechnicalException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BusinessException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", name = "BusinessException")
    public JAXBElement<BusinessException> createBusinessException(BusinessException value) {
        return new JAXBElement<BusinessException>(_BusinessException_QNAME, BusinessException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Update }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", name = "update")
    public JAXBElement<Update> createUpdate(Update value) {
        return new JAXBElement<Update>(_Update_QNAME, Update.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Ping }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", name = "ping")
    public JAXBElement<Ping> createPing(Ping value) {
        return new JAXBElement<Ping>(_Ping_QNAME, Ping.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/service/ch/mobi/maia/amw/v1_0/MaiaAmwFederationService/datatype", name = "updateResponse")
    public JAXBElement<UpdateResponse> createUpdateResponse(UpdateResponse value) {
        return new JAXBElement<UpdateResponse>(_UpdateResponse_QNAME, UpdateResponse.class, null, value);
    }

}
