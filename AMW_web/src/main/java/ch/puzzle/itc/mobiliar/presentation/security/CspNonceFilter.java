package ch.puzzle.itc.mobiliar.presentation.security;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

@WebFilter(filterName = "CspNonceFilter", urlPatterns = {"/*"})
public class CspNonceFilter implements Filter {

    private static final String CSP_HEADER_KEY = "Content-Security-Policy";

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
        String nonce = Nonce.next().toString();
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
        return String.format("default-src 'self';" +
                " script-src 'strict-dynamic' 'nonce-%s' 'unsafe-eval';", nonce) +
                " connect-src 'self';" +
                " script-src-attr 'unsafe-inline';" +
                " frame-ancestors 'none'; base-uri 'self'; object-src 'none';" +
                " style-src 'self' 'unsafe-inline';";
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
