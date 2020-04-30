package com.bassboy.configuration;

import com.bassboy.services.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Component
    public class InterceptorAppConfig extends WebMvcConfigurerAdapter {
        @Autowired
        RequestInterceptor schemaConverterServiceInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(schemaConverterServiceInterceptor);
        }
    }
