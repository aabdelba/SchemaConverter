package com.bassboy.schemaevolver;

import com.bassboy.common.ConfigProp;
import com.bassboy.common.RwUtils;
import org.apache.avro.Schema;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SchemaEvolverMain {

    private ConfigProp configProp;

    // DEBUG
    public static void main(String[] args) throws IOException, SchemaEvolverException, InvalidEntryException {

        ConfigProp configProp = ConfigProp.getInstance();
        String inputDir = System.getProperty("user.dir")+configProp.getProperty("input.dir");

        String oldSchemaFile = inputDir + "debug/schema/schema1.avsc";
        String newSchemaFile = inputDir + "debug/schema/schema2.avsc";
        String oldJsonFile = inputDir+"debug/record/record.json";
        String renamedFile = inputDir+"debug/schema/renamedFields.txt";

        SchemaEvolverMain sc = new SchemaEvolverMain();
        sc.convertDataAndPlaceInOutputDir(oldSchemaFile,newSchemaFile,oldJsonFile,renamedFile);
    }

    public void convertDataAndPlaceInOutputDir(String oldSchemaFile, String newSchemaFile, String oldJsonFile, String renamedFile) throws IOException, SchemaEvolverException, InvalidEntryException {
        convertDataAndPlaceInOutputDir(new File(oldSchemaFile),new File(newSchemaFile),new File(oldJsonFile),new File(renamedFile));
    }

    public void convertDataAndPlaceInOutputDir(File oldSchemaFile, File newSchemaFile, File oldJsonFile, File renamedFile) throws IOException, SchemaEvolverException, InvalidEntryException {
        configProp = ConfigProp.getInstance();
        String outputDir = System.getProperty("user.dir") + configProp.getProperty("output.dir");

        //create schema objects
        RecordObject oldRecord = new RecordObject(oldSchemaFile);
        oldRecord.setRecord(RwUtils.convertFileContentToString(oldJsonFile));
        RecordObject newRecord = new RecordObject(newSchemaFile);
        HashMap<String,String> renamedFields;
        if(renamedFile.exists()) renamedFields = RwUtils.getMapFromEqualSignNewlineSeparatedFile(renamedFile);
        else renamedFields = new HashMap<>();

        //run BFS - schemas need conditioning before parsing the JSON into newRecord
        conditionSchemaUsingBFS(oldRecord,newRecord,renamedFields);

        //read old json into a record object
        newRecord.setRecord(oldRecord.getSchema(), oldRecord.getRecord());// read in the input to record object
        System.out.println("\nJSON record in old schema:\n"+ oldRecord.getRecord());
        System.out.println("\nJSON record in new schema:\n"+newRecord.getRecord()+"\n");

        //write new json into avro file
        RwUtils.writeStringToFile(outputDir+"json/"+oldJsonFile.getName(),newRecord.getRecord().toString());
        newRecord.writeAvroToFile(outputDir+"avro/"+oldJsonFile.getName().substring(0,oldJsonFile.getName().indexOf('.'))+".avro");// write record object into .avro file
    }

    private void conditionSchemaUsingBFS(RecordObject oldRecord, RecordObject newRecord, HashMap<String, String> renamedFields) throws IOException, SchemaEvolverException {

        //use singleton instance of SchemaConditioner
        BfsConditioner schemaConditioner = BfsConditioner.getInstance(oldRecord, newRecord, renamedFields);
        schemaConditioner.startConversion();// this is the method to start the breadth-first process

        Schema.Parser parser = new Schema.Parser();//create new parser
        oldRecord.setSchema(parser.parse(schemaConditioner.getOldSchema().toString()));//set updated old schema
        oldRecord.setRecord(schemaConditioner.getOldJson().toString());//set updated old json that has a modified structure if there were array wrappings/unwrappings in the schemas
        parser = new Schema.Parser();//reset the parser
        newRecord.setSchema(parser.parse(schemaConditioner.getLatestSchema().toString()));//set updated latest schema

//        System.out.println("\nDEBUG OLD:\n"+ oldSchema.getSchema());
//        System.out.println("DEBUG NEW:\n"+ newSchema.getSchema());

    }

}

