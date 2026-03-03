package com.epam.gym.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.epam.gym.core")
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.epam.gym.core.repository")
public class AppConfig {

}
