package com.bassboy.schemaevolver;

import com.bassboy.common.RwUtils;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class JsonSchemaEvolver implements SchemaEvolver {


    // if @Value is used here instead of in a constructor:
    // when runnning unit tests, we detect reflection issues only at runtime (ex: fields not existing any longer)
//    @Value("${custom.schemaEvolver.ioDir}")
    private String ioDir;

    public void setIoDir(String ioDir) {
        this.ioDir = ioDir;
    }

    public String getIoDir() {
        return ioDir;
    }

    public JsonSchemaEvolver(String ioDir) {
        this.ioDir = ioDir;
    }

    public void convertDataAndPlaceInOutputDir(String oldSchemaFile, String newSchemaFile, String oldJsonFile, String renamedFile, String outputDir) throws IOException, SchemaEvolverException, InvalidSchemaEntryException {
        convertDataAndPlaceInOutputDir(new File(oldSchemaFile),new File(newSchemaFile),new File(oldJsonFile),new File(renamedFile), outputDir);
    }

    public void convertDataAndPlaceInOutputDir(File oldSchemaFile, File newSchemaFile, File oldJsonFile, File renamedFile, String outputDir) throws IOException, SchemaEvolverException, InvalidSchemaEntryException {

        //create schema objects
        EvolvingRecords oldRecords = new EvolvingRecords(oldSchemaFile);
        oldRecords.addRecord(oldJsonFile);
        EvolvingRecords newRecords = new EvolvingRecords(newSchemaFile);
        HashMap<String,String> renamedFields;
        if(renamedFile.exists()) renamedFields = RwUtils.getMapFromEqualSignNewlineSeparatedFile(renamedFile);
        else renamedFields = new HashMap<>();

        //run BFS - schemas need conditioning before parsing the JSON into newRecord
        conditionSchemaUsingBFS(oldRecords,newRecords,renamedFields);

        // now that schemas are conditioned, use AVRO library to read in the records in the new schema
        System.out.println("\nJSON record in old schema:\n" + RwUtils.convertFileContentToString(oldJsonFile));
        for (GenericData.Record oldRecord:oldRecords.getRecords()) {
            try {
                GenericData.Record newRecord = newRecords.addRecord(oldRecords.getSchema(), oldRecord);
                System.out.println("\nJSON record in new schema:\n" + newRecord + "\n");
            }catch(Exception unhandledScenario){
                unhandledScenario.printStackTrace();
                throw new SchemaEvolverException("Error due to unimplemented feature.\n"+unhandledScenario.getMessage());
            }
        }
        newRecords.writeToResultFiles(outputDir,oldJsonFile.getName().substring(0,oldJsonFile.getName().indexOf('.')));
    }

    private void conditionSchemaUsingBFS(EvolvingRecords oldRecords, EvolvingRecords newRecords, HashMap<String, String> renamedFields) throws IOException, SchemaEvolverException {

        //use singleton instance of SchemaConditioner
        BfsConditioner schemaConditioner = BfsConditioner.getInstance(oldRecords, newRecords, renamedFields);
        schemaConditioner.startConversion();// this is the method to start the breadth-first process

        Schema.Parser parser = new Schema.Parser();//create new parser
        oldRecords.setSchema(parser.parse(schemaConditioner.getOldSchema().toString()));//set updated old schema
        oldRecords.setRecords(schemaConditioner.getJsonRecords());//set updated old json that has a modified structure if there were array wrappings/unwrappings in the schemas
        parser = new Schema.Parser();//reset the parser
        newRecords.setSchema(parser.parse(schemaConditioner.getLatestSchema().toString()));//set updated latest schema
    }

}

