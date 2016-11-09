package ch.puzzle.itc.mobiliar.stp.ws.urlprovider;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class NoAuthUrlProvider implements UrlConnectionProvider {

	@Override
	public URLConnection openURLConnection(URL targetUrl) throws IOException {
		return targetUrl.openConnection();
	}

    @Override public String getDebugInformation() {
	   return "no authorization";
    }

}
