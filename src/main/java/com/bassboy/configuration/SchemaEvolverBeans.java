package com.bassboy.configuration;

import com.bassboy.common.ConfigProp;
import com.bassboy.services.SchemaResourceManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

@Configuration
public class SchemaEvolverBeans {

    @Primary
    @Bean
    public SchemaResourceManager initSchemaResourceManager() throws IOException {
        SchemaResourceManager srm = new SchemaResourceManager();
//        srm.clearDirectories();
        return srm;
    }

}
