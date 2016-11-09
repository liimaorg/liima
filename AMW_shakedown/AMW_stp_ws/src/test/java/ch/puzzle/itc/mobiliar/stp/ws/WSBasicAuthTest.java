package ch.puzzle.itc.mobiliar.stp.ws;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ch.puzzle.itc.mobiliar.stp.ws.exceptions.WSSTPException;

@Ignore("Dependency on the environment. Thought to be executed manually")
public class WSBasicAuthTest {

	WS_STP stp;

	String[] validParameters = new String[] { "http://romai.rz.puzzle.ch:8080/AMW_wsauth_test/TestService", "BASIC", "2000", "http://webservice.mobiliar.itc.puzzle.ch/", "mobi", "mobi"};

	@Test
	public void testRomaiWithBasicAuth() {
		WS_STP stp = new WS_STP(validParameters);
		stp.execute();
	}
	
	@Test(expected = IOException.class)
	public void testRequestWebserviceNOK() throws IOException {
		WS_STP stp = new WS_STP(validParameters);
		stp.requestWebservice("invalidRequest");
	}

	@Test
	public void testRequestWebservice() throws IOException {
		WS_STP stp = new WS_STP(validParameters);
		String response = stp.requestWebservice(stp.createWSDLMessage());
		Assert.assertNotNull(response);
		Assert.assertTrue(stp.validateWebService(response));
	}

	@Ignore
	@Test(expected=WSSTPException.class)
	public void testWithLowTimeout() {
		String[] lowTimeout = Arrays.copyOf(validParameters, validParameters.length);
		// We can't take 0 (because this means infinite timeout) but probability that the server responds
		// within one millisecond is rather low.
		lowTimeout[2] = "1";
		WS_STP stp = new WS_STP(lowTimeout);
		stp.execute();
	}
	
	@Test(expected=WSSTPException.class)
	public void testRomaiWithWrongUrl() {
		String[] wrongUrl = Arrays.copyOf(validParameters, validParameters.length);
		wrongUrl[0] = "http://somewhereelse/";
		WS_STP stp = new WS_STP(wrongUrl);
		stp.execute();
	}
	
	
	@Test(expected=WSSTPException.class)
	public void testRomaiWithInvalidCertPassword() {
		String[] wrongUrl = Arrays.copyOf(validParameters, validParameters.length);
		wrongUrl[5] = "invalidPassword";
		WS_STP stp = new WS_STP(wrongUrl);
		stp.execute();
	}

}
