package com.bassboy.schemaconversion;

import com.bassboy.utils.ConfigProp;
import com.bassboy.utils.RwUtils;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SchemaConverterMain {

    private ConfigProp configProp;

    // DEBUG
    public static void main(String[] args) throws IOException, SchemaConverterException {

        ConfigProp configProp = ConfigProp.getInstance();
        String inputDir = System.getProperty("user.dir")+configProp.getProperty("input.dir");

        String oldSchemaFile = inputDir + "debug/schema/schema1.avsc";
        String newSchemaFile = inputDir + "debug/schema/schema2.avsc";
        String oldJsonFile = inputDir+"debug/record/record.json";
        String renamedFile = inputDir+"debug/schema/renamedFields.txt";

        SchemaConverterMain sc = new SchemaConverterMain();
        sc.matchToSchema(oldSchemaFile,newSchemaFile,oldJsonFile,renamedFile);
    }

    public void matchToSchema(String oldSchemaFile, String newSchemaFile, String oldJsonFile, String renamedFile) throws IOException, SchemaConverterException {
        matchToSchema(new File(oldSchemaFile),new File(newSchemaFile),new File(oldJsonFile),new File(renamedFile));
    }

    public void matchToSchema(File oldSchemaFile, File newSchemaFile, File oldJsonFile, File renamedFile) throws IOException, SchemaConverterException {
        configProp = ConfigProp.getInstance();
        String inputDir = System.getProperty("user.dir")+configProp.getProperty("input.dir");
        String outputDir = System.getProperty("user.dir") + configProp.getProperty("output.dir");

        //create schema objects
        SchemaObject oldSchema = new SchemaObject(oldSchemaFile);
        SchemaObject newSchema = new SchemaObject(newSchemaFile);

        //run BFS
        conditionSchemaUsingBFS(oldSchema,newSchema,oldJsonFile,renamedFile);

        //read old json into a record object
        GenericData.Record record = RwUtils.readDetailedJson(newSchema.getSchema(), oldSchema.getSchema(), oldSchema.getJson());// read in the input to record object
        System.out.println("\nJSON record in old schema:\n"+ oldSchema.getJson());
        System.out.println("\nJSON record in new schema:\n"+record);

        //write new json into avro file
        RwUtils.writeStringToFile(outputDir+"json/"+oldJsonFile.getName(),record.toString());
        RwUtils.writeAvroToFile(outputDir+"avro/"+oldJsonFile.getName().substring(0,oldJsonFile.getName().indexOf('.'))+".avro",
                                newSchema.getSchema(),
                                record);// write record object into .avro file
    }

    private void conditionSchemaUsingBFS(SchemaObject oldSchema, SchemaObject newSchema, File oldJsonFile, File renamedFile) throws IOException, SchemaConverterException {
        oldSchema.setJson(RwUtils.getJsonStringFromFile(oldJsonFile));

        HashMap<String,String> renamedFields = RwUtils.getMapFromEqualSignNewlineSeparatedFile(renamedFile);

        //use singleton instance of SchemaConditioner
        BfsConditioner schemaConditioner = BfsConditioner.getInstance(oldSchema, newSchema, oldSchema.getJson(), renamedFields);
        schemaConditioner.startConversion();// this is the method to start the breadth-first process

        Schema.Parser parser = new Schema.Parser();//create new parser
        oldSchema.setSchema(parser.parse(schemaConditioner.getOldSchema().toString()));//set updated old schema
        oldSchema.setJson(schemaConditioner.getOldJson().toString());//set updated old json that has a modified structure if there were array wrappings/unwrappings in the schemas
        parser = new Schema.Parser();//reset the parser
        newSchema.setSchema(parser.parse(schemaConditioner.getLatestSchema().toString()));//set updated latest schema

//        System.out.println("\nDEBUG OLD:\n"+ oldSchema.getSchema());
//        System.out.println("DEBUG NEW:\n"+ newSchema.getSchema());

    }

}

