package ch.puzzle.itc.mobiliar.stp.ws.urlprovider;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class BasicAuthUrlProvider implements UrlConnectionProvider {

	private final String userName;
	private final String password;	
	
	public BasicAuthUrlProvider(String userName, String password) {
		super();
		this.userName = userName;
		this.password = password;
	}

	@Override
	public URLConnection openURLConnection(URL targetUrl) throws IOException {
		HttpURLConnection con = (HttpURLConnection) targetUrl.openConnection();
		String basicAuth = userName + ":" + password;
		basicAuth = "Basic " + Base64.encodeBase64String(basicAuth.getBytes(StandardCharsets.UTF_8));
		con.setRequestProperty("Authorization", basicAuth);
		return con;
	}

    @Override
    public String getDebugInformation() {
	   return "Username: "+userName;
    }

}
