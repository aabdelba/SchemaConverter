package com.bassboy.configuration;

import com.bassboy.services.SchemaEvolverFactory;
import com.bassboy.services.SchemaResourceManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

@Configuration
public class SchemaEvolverBeans {

    // if @Value is used here instead of in a constructor:
    // when runnning unit tests, we detect reflection issues only at runtime (ex: fields not existing any longer)
//    @Value("${custom.schemaEvolver.ioDir}")
    private String ioDir;


    public SchemaEvolverBeans(@Value("${custom.schemaEvolver.ioDir}") String ioDir){
        this.ioDir = ioDir;
    }

    @Bean
    public SchemaEvolverFactory schemaEvolverFactory() {
        return new SchemaEvolverFactory();
    }

    @Primary
    @Bean(name="schemaResourceManager")
    public SchemaResourceManager schemaResourceManager() throws IOException {
        SchemaResourceManager srm = new SchemaResourceManager(
                schemaEvolverFactory().createSchemaEvolver("json",ioDir)
        );
        return srm;
    }

}
