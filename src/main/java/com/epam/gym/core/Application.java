package com.epam.gym.core;

import com.epam.gym.core.config.AppConfig;
import com.epam.gym.core.config.WebConfig;
import com.epam.gym.core.filter.TransactionIdFilter;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class Application {

    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.setBaseDir(System.getProperty("java.io.tmpdir"));
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", System.getProperty("java.io.tmpdir"));

        // Spring application context
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppConfig.class, WebConfig.class);

        // DispatcherServlet
        Wrapper servlet = Tomcat.addServlet(ctx, "dispatcher", new DispatcherServlet(context));
        servlet.setLoadOnStartup(1);
        ctx.addServletMappingDecoded("/*", "dispatcher");

        // TransactionIdFilter
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("transactionIdFilter");
        filterDef.setFilter(new TransactionIdFilter());
        ctx.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName("transactionIdFilter");
        filterMap.addURLPattern("/*");
        ctx.addFilterMap(filterMap);

        tomcat.start();
        tomcat.getServer().await();
    }
}
