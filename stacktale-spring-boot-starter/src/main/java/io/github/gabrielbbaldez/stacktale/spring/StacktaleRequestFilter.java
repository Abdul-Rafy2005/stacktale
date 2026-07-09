package io.github.gabrielbbaldez.stacktale.spring;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Opens every request's story with its HTTP line — {@code POST /orders/889/checkout} —
 * through the {@code stacktale.request} logger, which the auto-configuration wires
 * exclusively to the stacktale appender (your console never sees these lines). Server
 * errors additionally close the story with the status.
 */
public final class StacktaleRequestFilter implements Filter {

    static final String REQUEST_LOGGER = "stacktale.request";

    private static final Logger log = LoggerFactory.getLogger(REQUEST_LOGGER);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest http)) {
            chain.doFilter(request, response);
            return;
        }
        long start = System.currentTimeMillis();
        String line = http.getMethod() + " " + http.getRequestURI()
                + (http.getQueryString() != null ? "?" + http.getQueryString() : "");
        log.info("{}", line);
        try {
            chain.doFilter(request, response);
        } finally {
            if (response instanceof HttpServletResponse httpResponse && httpResponse.getStatus() >= 500) {
                log.info("{} → {} ({}ms)", line, httpResponse.getStatus(), System.currentTimeMillis() - start);
            }
        }
    }
}
