package ch.puzzle.itc.mobiliar.stp.ws;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.junit.Ignore;
import org.junit.Test;

public class KeyStoreTest {

	@Test
	@Ignore("Dependency on the environment. Thought to be executed manually")
	public void testLoadKeyStore() throws Exception {	
		FileInputStream trustfis = new FileInputStream("./src/test/resources/cacerts.jks");
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(trustfis, "changeit".toCharArray());
		trustfis.close();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustStore);
		KeyStore ks = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream("./src/test/resources/client.p12");
		ks.load(fis, "helloWorld".toCharArray());
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, "helloWorld".toCharArray());
		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);		
		URL url = new URL("https://romai.rz.puzzle.ch:8443/AMW_maia?wsdl");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con instanceof HttpsURLConnection) {
			((HttpsURLConnection) con).setSSLSocketFactory(sc.getSocketFactory());
				BufferedReader in = null;
			StringBuilder sb = new StringBuilder();
			try {				
				con.setDoInput(true);				
				String inputLine;
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);
				}
			} finally {
				if (in != null) {
					in.close();
				}
			}
			System.out.println(sb.toString());		
		}
	}

}
