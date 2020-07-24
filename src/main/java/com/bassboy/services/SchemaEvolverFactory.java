package com.bassboy.services;

import com.bassboy.schemaevolver.JsonSchemaEvolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// factory pattern if in the future different kinds of schema converters are added to this project
// eg. json schema evolver or xml schema evolver
public class SchemaEvolverFactory {

    public SchemaEvolverFactory(){}

    public JsonSchemaEvolver createSchemaEvolver(String type, String ioDir) {
        return new JsonSchemaEvolver(ioDir);
    }

}
