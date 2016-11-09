
package ch.mobi.xml.datatype.common.commons.v3;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ch.mobi.xml.datatype.common.commons.v3 package. 
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

    private final static QName _LocalizedMessage_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "LocalizedMessage");
    private final static QName _KeyValuePairList_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "KeyValuePairList");
    private final static QName _KeyValuePair_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "KeyValuePair");
    private final static QName _BusinessException_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "BusinessException");
    private final static QName _TechnicalException_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "TechnicalException");
    private final static QName _ValidationContext_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "ValidationContext");
    private final static QName _ValidationException_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "ValidationException");
    private final static QName _Message_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "Message");
    private final static QName _OpenTimePeriod_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "OpenTimePeriod");
    private final static QName _LocalizedString_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "LocalizedString");
    private final static QName _TimePeriod_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "TimePeriod");
    private final static QName _HistoryHeader_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "HistoryHeader");
    private final static QName _CallContext_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "CallContext");
    private final static QName _NotFoundException_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "NotFoundException");
    private final static QName _ISDVInfo_QNAME = new QName("http://xml.mobi.ch/datatype/common/Commons/v3", "ISDVInfo");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ch.mobi.xml.datatype.common.commons.v3
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BusinessException }
     * 
     */
    public BusinessException createBusinessException() {
        return new BusinessException();
    }

    /**
     * Create an instance of {@link ValidationException }
     * 
     */
    public ValidationException createValidationException() {
        return new ValidationException();
    }

    /**
     * Create an instance of {@link TechnicalException }
     * 
     */
    public TechnicalException createTechnicalException() {
        return new TechnicalException();
    }

    /**
     * Create an instance of {@link OpenTimePeriod }
     * 
     */
    public OpenTimePeriod createOpenTimePeriod() {
        return new OpenTimePeriod();
    }

    /**
     * Create an instance of {@link ISDVInfo }
     * 
     */
    public ISDVInfo createISDVInfo() {
        return new ISDVInfo();
    }

    /**
     * Create an instance of {@link Message }
     * 
     */
    public Message createMessage() {
        return new Message();
    }

    /**
     * Create an instance of {@link TimePeriod }
     * 
     */
    public TimePeriod createTimePeriod() {
        return new TimePeriod();
    }

    /**
     * Create an instance of {@link LocalizedString }
     * 
     */
    public LocalizedString createLocalizedString() {
        return new LocalizedString();
    }

    /**
     * Create an instance of {@link KeyValuePairList }
     * 
     */
    public KeyValuePairList createKeyValuePairList() {
        return new KeyValuePairList();
    }

    /**
     * Create an instance of {@link KeyValuePair }
     * 
     */
    public KeyValuePair createKeyValuePair() {
        return new KeyValuePair();
    }

    /**
     * Create an instance of {@link HistoryHeader }
     * 
     */
    public HistoryHeader createHistoryHeader() {
        return new HistoryHeader();
    }

    /**
     * Create an instance of {@link LocalizedMessage }
     * 
     */
    public LocalizedMessage createLocalizedMessage() {
        return new LocalizedMessage();
    }

    /**
     * Create an instance of {@link ValidationContext }
     * 
     */
    public ValidationContext createValidationContext() {
        return new ValidationContext();
    }

    /**
     * Create an instance of {@link CallContext }
     * 
     */
    public CallContext createCallContext() {
        return new CallContext();
    }

    /**
     * Create an instance of {@link NotFoundException }
     * 
     */
    public NotFoundException createNotFoundException() {
        return new NotFoundException();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LocalizedMessage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "LocalizedMessage")
    public JAXBElement<LocalizedMessage> createLocalizedMessage(LocalizedMessage value) {
        return new JAXBElement<LocalizedMessage>(_LocalizedMessage_QNAME, LocalizedMessage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link KeyValuePairList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "KeyValuePairList")
    public JAXBElement<KeyValuePairList> createKeyValuePairList(KeyValuePairList value) {
        return new JAXBElement<KeyValuePairList>(_KeyValuePairList_QNAME, KeyValuePairList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link KeyValuePair }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "KeyValuePair")
    public JAXBElement<KeyValuePair> createKeyValuePair(KeyValuePair value) {
        return new JAXBElement<KeyValuePair>(_KeyValuePair_QNAME, KeyValuePair.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BusinessException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "BusinessException")
    public JAXBElement<BusinessException> createBusinessException(BusinessException value) {
        return new JAXBElement<BusinessException>(_BusinessException_QNAME, BusinessException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TechnicalException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "TechnicalException")
    public JAXBElement<TechnicalException> createTechnicalException(TechnicalException value) {
        return new JAXBElement<TechnicalException>(_TechnicalException_QNAME, TechnicalException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidationContext }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "ValidationContext")
    public JAXBElement<ValidationContext> createValidationContext(ValidationContext value) {
        return new JAXBElement<ValidationContext>(_ValidationContext_QNAME, ValidationContext.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidationException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "ValidationException")
    public JAXBElement<ValidationException> createValidationException(ValidationException value) {
        return new JAXBElement<ValidationException>(_ValidationException_QNAME, ValidationException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Message }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "Message")
    public JAXBElement<Message> createMessage(Message value) {
        return new JAXBElement<Message>(_Message_QNAME, Message.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OpenTimePeriod }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "OpenTimePeriod")
    public JAXBElement<OpenTimePeriod> createOpenTimePeriod(OpenTimePeriod value) {
        return new JAXBElement<OpenTimePeriod>(_OpenTimePeriod_QNAME, OpenTimePeriod.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LocalizedString }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "LocalizedString")
    public JAXBElement<LocalizedString> createLocalizedString(LocalizedString value) {
        return new JAXBElement<LocalizedString>(_LocalizedString_QNAME, LocalizedString.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimePeriod }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "TimePeriod")
    public JAXBElement<TimePeriod> createTimePeriod(TimePeriod value) {
        return new JAXBElement<TimePeriod>(_TimePeriod_QNAME, TimePeriod.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HistoryHeader }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "HistoryHeader")
    public JAXBElement<HistoryHeader> createHistoryHeader(HistoryHeader value) {
        return new JAXBElement<HistoryHeader>(_HistoryHeader_QNAME, HistoryHeader.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CallContext }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "CallContext")
    public JAXBElement<CallContext> createCallContext(CallContext value) {
        return new JAXBElement<CallContext>(_CallContext_QNAME, CallContext.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotFoundException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "NotFoundException")
    public JAXBElement<NotFoundException> createNotFoundException(NotFoundException value) {
        return new JAXBElement<NotFoundException>(_NotFoundException_QNAME, NotFoundException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ISDVInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.mobi.ch/datatype/common/Commons/v3", name = "ISDVInfo")
    public JAXBElement<ISDVInfo> createISDVInfo(ISDVInfo value) {
        return new JAXBElement<ISDVInfo>(_ISDVInfo_QNAME, ISDVInfo.class, null, value);
    }

}
