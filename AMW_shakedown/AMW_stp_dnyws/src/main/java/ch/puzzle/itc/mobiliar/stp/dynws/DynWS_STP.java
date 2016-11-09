package ch.puzzle.itc.mobiliar.stp.dynws;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduitConfigurer;

public class DynWS_STP {

	private static final String PING_METHOD = "ping";
	private static final Level CXF_LOG_LEVEL = Level.SEVERE;
	private String wsdl;
	private String user;
	private String password;
	private Client client;

	public DynWS_STP(String wsdl, String user, String password) {
		this.wsdl = wsdl;
		this.user = user;
		this.password = password;
	}

	private void initCxf() {
		// turn down cxf Logging
		Logger globalLogger = Logger.getLogger("");
		globalLogger.setLevel(CXF_LOG_LEVEL);

		Bus bus = CXFBusFactory.getThreadDefaultBus();


		DynWSAuthHTTPConduitConfigurer conf = new DynWSAuthHTTPConduitConfigurer(user, password);
		bus.setExtension(conf, HTTPConduitConfigurer.class);
		JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance(bus);
		client = dcf.createClient(wsdl);

	}

	public void callPing() throws Exception {
		Object[] result = null;
		result = client.invoke(PING_METHOD);

		if (result.length != 0) {
			throw new Exception();
		}
		// if no exception is throw, everything worked!
		System.out.println("Successfully called ping");
	}

	public void run() throws Exception {
		System.out.println("Trying to called ping of webservice " + wsdl + " with user " + user);
		initCxf();
		callPing();
	}

	@SuppressWarnings("static-access")
	public static CommandLine parseArgument(String[] args) {
		CommandLineParser parser = new BasicParser();
		HelpFormatter lvFormater = new HelpFormatter();
		CommandLine line = null;
		Options options = new Options();

		options.addOption(OptionBuilder.withLongOpt("wsdl").withDescription("Wsdl URL of Webservice").hasArg().isRequired().create("w"));
		options.addOption(OptionBuilder.withLongOpt("user").withDescription("Webservice user").hasArg().create("u"));
		options.addOption(OptionBuilder.withLongOpt("password").withDescription("Webservice password").hasArg().create("p"));

		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			lvFormater.printHelp("DynWS_STP", options);
			System.exit(1);
		}

		if (line.hasOption("u") && !line.hasOption("p") || !line.hasOption("u") && line.hasOption("p")) {
			System.out.println("User and password have to be set");
			lvFormater.printHelp("DynWS_STP", options);
			System.exit(1);
		}

		return line;
	}
	
	/**
	 * Example call: --wsdl
	 * https://jsplb01v.umobi.mobicorp.test/IsdvService_v3_0/IsdvService?wsdl
	 * --user user --password password;
	 */
	public static void main(String[] args) throws Exception {
		// http://svn.codehaus.org/gmod/groovyws/branches/0.3/src/main/java/groovyx/net/ws/WSClient.java
		DynWS_STP stp;
		CommandLine line = parseArgument(args);
		
		stp = new DynWS_STP(line.getOptionValue("w"), line.getOptionValue("u"), line.getOptionValue("p"));

		try {
			stp.run();
		} catch (Exception e) {
			System.out.println("Could not call ping on webservice");
			throw e;
		}

	}

}
