package com.epam.gym.core;

import com.epam.gym.core.config.AppConfig;
import com.epam.gym.core.config.WebConfig;
import com.epam.gym.core.filter.TransactionIdFilter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { AppConfig.class, WebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);
        FilterRegistration.Dynamic filter = servletContext.addFilter(
                "transactionIdFilter", TransactionIdFilter.class);
        filter.addMappingForUrlPatterns(null, false, "/*");
    }
}
