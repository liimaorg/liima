
package ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0 package. 
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

    private final static QName _ProvidedPortID_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", "ProvidedPortID");
    private final static QName _ApplicationReleaseBinding_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", "ApplicationReleaseBinding");
    private final static QName _ConsumedPort_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", "ConsumedPort");
    private final static QName _Application_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", "Application");
    private final static QName _ProvidedPort_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", "ProvidedPort");
    private final static QName _ApplicationPayload_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", "ApplicationPayload");
    private final static QName _PropertyDeclaration_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", "PropertyDeclaration");
    private final static QName _ApplicationID_QNAME = new QName("http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", "ApplicationID");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ProvidedPort }
     * 
     */
    public ProvidedPort createProvidedPort() {
        return new ProvidedPort();
    }

    /**
     * Create an instance of {@link PropertyDeclaration }
     * 
     */
    public PropertyDeclaration createPropertyDeclaration() {
        return new PropertyDeclaration();
    }

    /**
     * Create an instance of {@link ConsumedPort }
     * 
     */
    public ConsumedPort createConsumedPort() {
        return new ConsumedPort();
    }

    /**
     * Create an instance of {@link ApplicationPayload }
     * 
     */
    public ApplicationPayload createApplicationPayload() {
        return new ApplicationPayload();
    }

    /**
     * Create an instance of {@link ApplicationReleaseBinding }
     * 
     */
    public ApplicationReleaseBinding createApplicationReleaseBinding() {
        return new ApplicationReleaseBinding();
    }

    /**
     * Create an instance of {@link ProvidedPortID }
     * 
     */
    public ProvidedPortID createProvidedPortID() {
        return new ProvidedPortID();
    }

    /**
     * Create an instance of {@link Application }
     * 
     */
    public Application createApplication() {
        return new Application();
    }

    /**
     * Create an instance of {@link ApplicationID }
     * 
     */
    public ApplicationID createApplicationID() {
        return new ApplicationID();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProvidedPortID }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", name = "ProvidedPortID")
    public JAXBElement<ProvidedPortID> createProvidedPortID(ProvidedPortID value) {
        return new JAXBElement<ProvidedPortID>(_ProvidedPortID_QNAME, ProvidedPortID.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ApplicationReleaseBinding }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", name = "ApplicationReleaseBinding")
    public JAXBElement<ApplicationReleaseBinding> createApplicationReleaseBinding(ApplicationReleaseBinding value) {
        return new JAXBElement<ApplicationReleaseBinding>(_ApplicationReleaseBinding_QNAME, ApplicationReleaseBinding.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConsumedPort }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", name = "ConsumedPort")
    public JAXBElement<ConsumedPort> createConsumedPort(ConsumedPort value) {
        return new JAXBElement<ConsumedPort>(_ConsumedPort_QNAME, ConsumedPort.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Application }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", name = "Application")
    public JAXBElement<Application> createApplication(Application value) {
        return new JAXBElement<Application>(_Application_QNAME, Application.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProvidedPort }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", name = "ProvidedPort")
    public JAXBElement<ProvidedPort> createProvidedPort(ProvidedPort value) {
        return new JAXBElement<ProvidedPort>(_ProvidedPort_QNAME, ProvidedPort.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ApplicationPayload }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", name = "ApplicationPayload")
    public JAXBElement<ApplicationPayload> createApplicationPayload(ApplicationPayload value) {
        return new JAXBElement<ApplicationPayload>(_ApplicationPayload_QNAME, ApplicationPayload.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PropertyDeclaration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", name = "PropertyDeclaration")
    public JAXBElement<PropertyDeclaration> createPropertyDeclaration(PropertyDeclaration value) {
        return new JAXBElement<PropertyDeclaration>(_PropertyDeclaration_QNAME, PropertyDeclaration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ApplicationID }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/ch/mobi/maia/amw/MaiaAmwFederation/v1_0", name = "ApplicationID")
    public JAXBElement<ApplicationID> createApplicationID(ApplicationID value) {
        return new JAXBElement<ApplicationID>(_ApplicationID_QNAME, ApplicationID.class, null, value);
    }

}
