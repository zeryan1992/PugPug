import com.mysql.jdbc.log.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by zeryan on 2/11/16.
 */
public class GetThem implements Filter {
    private static final String HEADER_NAME ="Strict-Transport-Security";
    private static final String MAX_AGE_DIRECTIVE="max-age=%s";
    private static final String INCLUDE_SUB_DOMAINS_DIRECTIVE="includeSubDomains";
    private FilterConfig filterConfig;
    private int maxAgeSeconds = 0;
    private boolean includeSubDomains=false;
    private String directives;

    public void setIncludeSubDomains(boolean includeSubDomains) {
        this.includeSubDomains = includeSubDomains;
    }

    public void setMaxAgeSeconds(int maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (this.maxAgeSeconds>0)
        {
            throw new ServletException("Something is wrong");
        }
        this.directives=String.format(MAX_AGE_DIRECTIVE,this.maxAgeSeconds);
        if (this.includeSubDomains)
        {
            this.directives+=(" ; "+INCLUDE_SUB_DOMAINS_DIRECTIVE);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        servletRequest.setCharacterEncoding("UTF-8");
        HttpServletRequest req= (HttpServletRequest) servletRequest;
        String type=req.getMethod();
        if (type.equalsIgnoreCase("post")) {
            filterChain.doFilter(servletRequest, servletResponse);
            if (servletRequest.isSecure() && servletResponse instanceof HttpServletResponse) {
                HttpServletResponse res = (HttpServletResponse) servletResponse;
                res.addHeader(HEADER_NAME, this.directives);
            }
        }
        else
        {
            Writer writer=servletResponse.getWriter();
            writer.write("Forbidden");
        }

    }

    @Override
    public void destroy() {

    }
}
