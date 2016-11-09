package ch.puzzle.itc.mobiliar.stp.ws;

import ch.puzzle.itc.mobiliar.stp.ws.exceptions.WSSTPException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class WSTest {

	WS_STP stp;
	String[] validParams = new String[]{"http://romai.rz.puzzle.ch:8080/AMW_maia/AmwDataSinkServiceImpl", "NONE", "2000", "http://xml.mobi.ch/service/amw/AmwDataSinkService/datatype/v1"};


	@Before
	public void setup(){
		stp = new WS_STP(validParams);
	}


	@Test(expected=WSSTPException.class)
	public void testWSStpMissingArguments(){
		stp = new WS_STP();
	}


	@Test
	public void testCreateWSDLRequestFromFile() throws IOException {
		String wsdl = stp.createWSDLMessage();
		Assert.assertNotNull(wsdl);
		Assert.assertTrue(wsdl.startsWith("<"));
	}

	@Test
	public void testReadExpectedResponseFromFile() throws IOException {
		String response = stp.readExpectedResponse();
		Assert.assertNotNull(response);
	}

	@Ignore("Dependency on the environment. Thought to be executed manually")
	@Test
	public void testSendPingRequest() throws IOException {
		String response = stp.requestWebservice(stp.createWSDLMessage());
		Assert.assertNotNull(response);
	}

	@Test(expected=WSSTPException.class)
	public void testSendPingRequestNOK() throws IOException {
		stp = new WS_STP("http://nonexistingwebservice", "NONE", "2000");
	}


	@Test
	public void testValidateResponse() throws IOException {
		String response = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns3:pingResponse xmlns:ns4=\"http://xml.mobi.ch/datatype/common/Commons/v3\" xmlns:ns3=\"http://xml.mobi.ch/service/amw/AmwDataSinkService/datatype/v1\" xmlns:ns2=\"http://xml.mobi.ch/datatype/amw/AmwDataSink/v1\"/></soap:Body></soap:Envelope>";
		Assert.assertTrue(stp.validateWebService(response));
	}


	@Test
	public void testValidateResponseNOK() throws IOException {
		String wrongResponse = "sdfjdklsjflksjlfkjsdlj";
		Assert.assertFalse(stp.validateWebService(wrongResponse));
	}

    @Test
    public void testConstructDebugMessage() throws Exception {
	  	stp = new WS_STP(validParams);
	   	String s = stp.constructDebugMessage();
    		Assert.assertTrue("The debug message should contain the endpoint of the webservice but it does not.", s.contains("http://romai.rz.puzzle.ch:8080/AMW_maia/AmwDataSinkServiceImpl"));
    }

    @Test
    public void testConstructDebugMessageInExecute() throws Exception {
	   PrintStream originalStream = System.out;
	   try {
		  ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		  PrintStream instrumentalizedStream = new PrintStream(outContent);
		  System.setOut(instrumentalizedStream);
		  WS_STP stp = Mockito.spy(new WS_STP(validParams));
		  Mockito.doReturn("foo").when(stp).requestWebservice(Mockito.anyString());
		  Mockito.doReturn(true).when(stp).validateWebService(Mockito.anyString());
		  stp.execute();
		  String sysout = outContent.toString();
		  Assert.assertTrue("The debug message should contain the endpoint of the webservice but it does not.", sysout.contains(validParams[0]));
	   }
	   finally {
		  //Restore the original output stream
		  System.setOut(originalStream);
	   }
    }

    @Test
    public void testHandleWSSTPException() throws Exception {
	   PrintStream originalStream = System.out;
	   try {
		  ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		  PrintStream instrumentalizedStream = new PrintStream(outContent);
		  System.setOut(instrumentalizedStream);

		  WSSTPException exception = new WSSTPException("Someerror");
		  WS_STP stp = new WS_STP(validParams);
		  WS_STP.handleWSSTPException(stp, exception);

		  String sysout = outContent.toString();
		  Assert.assertTrue("The debug message should contain the error of the exception but it does not.", sysout.contains("Someerror"));

		  Assert.assertTrue("The debug message should contain the endpoint of the webservice but it does not.", sysout.contains(validParams[0]));
	   }
	   finally {
		  //Restore the original output stream
		  System.setOut(originalStream);
	   }
    }
}
