package com.bassboy.schemaevolver;

import com.bassboy.services.SchemaEvolverFactory;

import java.io.IOException;

public class SchemaEvolverMain {

    private static final String ioDir = System.getProperty("user.dir")+"/SchemaEvolverIO/debug";

    public static void main(String[] args) throws IOException, SchemaEvolverException, InvalidSchemaEntryException {

        String inputDir = ioDir+"/input/";
        String outputDir = ioDir+"/output/";

        String oldSchemaFile = inputDir + "schema/demo_objectToArray.avsc";
        String newSchemaFile = inputDir + "schema/demo_arrayToObject.avsc";
        String oldJsonFile = inputDir+"record/demoWithArray.json";
        String renamedFile = inputDir+"schema/demo_renamedFields.txt";

        SchemaEvolverFactory schemaEvolverFactory = new SchemaEvolverFactory();
        JsonSchemaEvolver schemaEvolver = schemaEvolverFactory.createSchemaEvolver("json",ioDir);
        schemaEvolver.convertDataAndPlaceInOutputDir(oldSchemaFile,newSchemaFile,oldJsonFile,renamedFile, outputDir);
    }
}
