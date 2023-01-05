package ch.puzzle.itc.mobiliar.presentation.security;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@WebFilter(filterName = "CspNonceFilter", urlPatterns = {"/*"})
public class CspNonceFilter implements Filter {

    private static final int NONCE_SIZE = 32;
    //TODO remove -Report-Only to enforce policy
    private static final String CSP_HEADER_KEY = "Content-Security-Policy-Report-Only";
    private static final String HASHES = "'sha256-56Xl7F05vDGrlZ0zsrpH5ur8snvD2VXeGJW6hJvvbxU=' 'sha256-JnUQNzIwUQpUTIqGq+F8FFSf1//vhdKJyY/8LSNVWh0='";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        CharResponseWrapper charResponseWrapper = new CharResponseWrapper((HttpServletResponse) servletResponse) {
            @Override
            public void setHeader(String name, String value) {
                httpResponse.setHeader(name, value);
            }
        };
        String nonce = getNonce();
        httpResponse.setHeader(CSP_HEADER_KEY, getCspHeader(nonce));

        filterChain.doFilter(servletRequest, charResponseWrapper);
        if (charResponseWrapper.toString().contains("script")) {
            PrintWriter out = servletResponse.getWriter();
            CharArrayWriter caw = new CharArrayWriter();
            caw.write(charResponseWrapper.toString().replace("<script", "<script nonce=\"" + nonce + "\"" ));
            charResponseWrapper.setContentLength(caw.toString().length());
            out.write(caw.toString());
            out.close();
        }

    }

    private String getCspHeader(String nonce) {
        return String.format("default-src 'self'; script-src 'self' 'nonce-%s' %s 'unsafe-eval'; ", nonce, HASHES) +
                "connect-src 'self'; script-src-attr 'unsafe-inline'; frame-ancestors 'none'; base-uri 'none';" +
                " style-src 'self' 'unsafe-inline';";
    }

    private String getNonce() {
        byte[] nonce = new byte[NONCE_SIZE];
        new SecureRandom().nextBytes(nonce);

        return Base64.getEncoder().encodeToString(nonce);
    }

    @Override
    public void destroy() {
    }

    public static class CharResponseWrapper extends
            HttpServletResponseWrapper {
        private CharArrayWriter output;

        public String toString() {
            return output.toString();
        }

        public CharResponseWrapper(HttpServletResponse response) {
            super(response);
            output = new CharArrayWriter();

        }

        public PrintWriter getWriter() {
            return new PrintWriter(output);
        }
    }
}
