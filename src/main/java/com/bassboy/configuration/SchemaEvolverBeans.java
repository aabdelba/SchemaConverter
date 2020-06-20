package com.bassboy.configuration;

import com.bassboy.services.SchemaResourceManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

@Configuration
public class SchemaEvolverBeans {

    @Value("${custom.schemaEvolver.ioDir}")
    private String ioDir;

    @Primary
    @Bean(name="schemaResourceManager")
    public SchemaResourceManager schemaResourceManager() throws IOException {
        SchemaResourceManager srm = SchemaResourceManager.getInstance(ioDir);
        srm.setDirectories();
//        srm.clearDirectories();
        return srm;
    }

}
