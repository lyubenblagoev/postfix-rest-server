package com.lyubenblagoev.postfixrest.configuration;

import com.lyubenblagoev.postfixrest.controller.DomainAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DomainAccessConfiguration implements WebMvcConfigurer {

    private final DomainAccessInterceptor domainAccessInterceptor;

    public DomainAccessConfiguration(DomainAccessInterceptor domainAccessInterceptor) {
        this.domainAccessInterceptor = domainAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(domainAccessInterceptor);
    }
}
