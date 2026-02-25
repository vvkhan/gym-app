package com.epam.gym.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring configuration
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.epam.gym.core")
@EnableTransactionManagement
public class AppConfig {

}
