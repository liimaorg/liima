package ch.puzzle.itc.mobiliar.stp.ws.urlprovider;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public interface UrlConnectionProvider {

	/**
	 * Opens a URL connection for the given targetUrl. 
	 *  
	 * @param targetUrl
	 * @return
	 * @throws IOException
	 */
	URLConnection openURLConnection(URL targetUrl) throws IOException;

    /**
	*
	* @return additional (connection provider dependent) debug information which is provided to the AMW instance
	*/
     String getDebugInformation();
}
