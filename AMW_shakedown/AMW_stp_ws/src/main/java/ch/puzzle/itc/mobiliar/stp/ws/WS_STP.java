package ch.puzzle.itc.mobiliar.stp.ws;

import ch.puzzle.itc.mobiliar.stp.ws.exceptions.WSSTPException;
import ch.puzzle.itc.mobiliar.stp.ws.urlprovider.BasicAuthUrlProvider;
import ch.puzzle.itc.mobiliar.stp.ws.urlprovider.NoAuthUrlProvider;
import ch.puzzle.itc.mobiliar.stp.ws.urlprovider.UrlConnectionProvider;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Date;

/**
 * This shakedown test calls the ping method of a web service endpoint. It supports the following
 * authentication methods: - NONE (to access an unprotected web service) - CLIENT-CERT (to access a web
 * service protected through certificates)
 */
public class WS_STP {

	/**
	 * @param args
	 *             - the first argument defines which authentication method shall be used. The following
	 *             parameters are authentication specific. for optional parameters, use "default"
	 */
	public static void main(String[] args) {
	     WS_STP wsStp = null;
	     try {
			wsStp = new WS_STP(args);
			wsStp.execute();
		}
		catch (WSSTPException e) {
		     handleWSSTPException(wsStp, e);
			System.exit(1);
		}
	}

     static void handleWSSTPException(WS_STP wsStp, WSSTPException e){
	    System.out.println(e.getMessage());
	    e.printStackTrace();
	    if(wsStp!=null) {
		   System.out.println("Webservice requested with the following parameters: " + wsStp
				   .constructDebugMessage());
	    }
	}

     String constructDebugMessage(){
	    StringBuilder sb = new StringBuilder();
	    if(url!=null) {
		   sb.append("Requested URL: ").append(url).append(", ");
	    }
	    sb.append("Ping-Namespace: ").append(pingNamespace).append(", ");
	    sb.append("Timeout: ").append(timeout).append(", ");
	    if(provider!=null){
		   sb.append("Authentification parameters: ").append(provider.getDebugInformation());
	    }
	    return sb.toString();
	}

	final URL url;
	final UrlConnectionProvider provider;
	final int timeout;
	final String pingNamespace;

	WS_STP(String... args) {
		if (args.length < 4) {
			throw new WSSTPException(
					"Missing parameters! You have to give at least the follwing parameters: ENDPOINTURL, AUTHENTICATION, TIMEOUT, PINGNAMESPACE!");
		}
		else {
			url = parseUrl(args[0]);
			String[] remainingArguments = args.length == 4 ? new String[0] : Arrays.copyOfRange(args, 4,
					args.length);
			provider = getUrlConnectionProvider(args[1], remainingArguments);
			timeout = parseTimeout(args[2]);
			pingNamespace = args[3];
		}
	}

	void execute() {
		try {
			long start = new Date().getTime();
			String s = requestWebservice(createWSDLMessage());
			long end = new Date().getTime();
		    	System.out.println(constructDebugMessage());
			if (validateWebService(s)) {
				System.out.println("Duration: " + (end - start) + "ms");
			}
			else {
				throw new WSSTPException("Ping request failed with the response " + s);
			}
		}
		catch (IOException e) {
			throw new WSSTPException("Webservice not reachable", e);
		}
	}

	UrlConnectionProvider getUrlConnectionProviderWithoutAuthentication(String... remainingArguments) {
		return new NoAuthUrlProvider();
	}
	
	/**
	 * @param remainingArguments - requires the following additional arguments: USERNAME, PASSWORD
	 * @return
	 */
	UrlConnectionProvider getUrlConnectionProviderWithBasicAuthentication(String... remainingArguments) {
		if(remainingArguments.length<2){
			throw new WSSTPException("Missing arguments! For basic authentication, the following additional parameters are required: USERNAME, PASSWORD");
		}
		return new BasicAuthUrlProvider(remainingArguments[0], remainingArguments[1]);
	}

	UrlConnectionProvider getUrlConnectionProvider(String authenticationMethod, String... remainingArguments) {
		UrlConnectionProvider provider;
		switch (authenticationMethod) {
		case "NONE":
			provider = getUrlConnectionProviderWithoutAuthentication(remainingArguments);
			break;
		case "BASIC":
			provider = getUrlConnectionProviderWithBasicAuthentication(remainingArguments);
			break;
		default:
			throw new WSSTPException(
					"Wrong parameter! The second parameter should define which authentication method should be used.");
		}
		return provider;
	}

	URL parseUrl(String url) {
		try {
			return new URL(url);
		}
		catch (MalformedURLException e) {
			throw new WSSTPException("The first parameter should be a valid URL!", e);
		}
	}

	int parseTimeout(String timeout) {
		try {
			return Integer.parseInt(timeout);
		}
		catch (NumberFormatException e) {
			throw new WSSTPException(
					"Wrong parameter! The third parameter should be a numeric value for the timeout in ms");
		}
	}

	String requestWebservice(String request) throws IOException {

		StringBuilder sb = new StringBuilder();
		URLConnection con = provider.openURLConnection(url);
		con.setConnectTimeout(timeout);
		con.setDoOutput(true);
		con.setDoInput(true);
		try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()))) {
			out.append(request);			
		}
		try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))){		
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				sb.append(inputLine);
			}		
		}		
		return sb.toString();
	}

	boolean validateWebService(String response) throws IOException {
		String respMatcher = readExpectedResponse();
		return response.matches(respMatcher);
	}

	String createWSDLMessage() throws IOException {
		return String.format(readFile("pingRequest.txt"), pingNamespace);
	}

	String readExpectedResponse() throws IOException {
		return readFile("pingResponse.txt");
	}

	String readFile(String file) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(file)));
			String read = r.readLine();
			while (read != null) {
				sb.append(read);
				read = r.readLine();
			}
		}
		finally {
			if (r != null) {
				r.close();
			}
		}
		return sb.toString();
	}

}
