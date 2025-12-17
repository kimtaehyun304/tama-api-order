package org.example.tamaapi.common.filter;

import jakarta.servlet.*;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class LogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig)  {}

    @Override
    public void destroy() {}
}
