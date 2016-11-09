package ch.puzzle.itc.mobiliar.stp.dynws;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitConfigurer;

/**
 * CXF Configuration for Basic Auth
 * 
 */
public class DynWSAuthHTTPConduitConfigurer implements HTTPConduitConfigurer {
   
    private final String username;
    private final String password;
   
    public DynWSAuthHTTPConduitConfigurer(String username, String password) {
        this.username = username;
        this.password = password;
    }
   
    @Override
    public void configure(String name, String address, HTTPConduit conduit) {
        if (username != null) {
	        AuthorizationPolicy ap = new AuthorizationPolicy();
	        ap.setUserName(username);
	        ap.setPassword(password);
	        conduit.setAuthorization(ap);
        }
        
        //override the ssl configuration of cxf and use the default implementation instead
        //CXF doesn't like pkcs12 keystore for some reason (Default key managers cannot be initialized: Invalid keystore format)
        TLSClientParameters tls = new TLSClientParameters();
        try {
			tls.setSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        conduit.setTlsClientParameters(tls);

    }
}
