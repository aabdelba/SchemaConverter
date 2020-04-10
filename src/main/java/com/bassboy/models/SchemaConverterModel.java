package com.bassboy.models;

import com.bassboy.schemaconversion.BfsConditioner;
import com.bassboy.schemaconversion.SchemaConverterException;
import com.bassboy.schemaconversion.SchemaObject;
import com.bassboy.utils.ConfigProp;
import com.bassboy.utils.GeneralUtils;
import com.bassboy.utils.RwUtils;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;

import java.io.IOException;
import java.util.*;

public class SchemaConverterModel {

    private ConfigProp configProp = ConfigProp.getInstance();
    private String latestSchemaFile;
    private String oldSchemaFile;
    private List<String> oldJsonFiles;
    private HashMap<String,String> renamedFieldsWithNoAliasMap;

    public String getLatestSchemaFile() {
        return latestSchemaFile;
    }

    public void setLatestSchemaFile(String latestSchemaFile) {
        this.latestSchemaFile = latestSchemaFile;
    }

    public String getOldSchemaFile() {
        return oldSchemaFile;
    }

    public void setOldSchemaFile(String oldSchemaFile) {
        this.oldSchemaFile = oldSchemaFile;
    }

    public List<String> getOldJsonFiles() {
        return oldJsonFiles;
    }

    public void setOldJsonFiles(List<String> oldJsonFiles) {
        this.oldJsonFiles = oldJsonFiles;
    }

    public HashMap<String, String> getRenamedFieldsWithNoAliasMap() {
        return renamedFieldsWithNoAliasMap;
    }

    public void setRenamedFieldsWithNoAliasMap(HashMap<String, String> renamedFieldsWithNoAliasMap) {
        this.renamedFieldsWithNoAliasMap = renamedFieldsWithNoAliasMap;
    }

    public SchemaConverterModel(String oldSchemaFile, String latestSchemaFile, List<String> oldJsonFiles, HashMap<String, String> renamedFieldsWithNoAliasMap) throws IOException {
        this.latestSchemaFile = latestSchemaFile;
        this.oldSchemaFile = oldSchemaFile;
        this.oldJsonFiles = oldJsonFiles;
        this.renamedFieldsWithNoAliasMap = renamedFieldsWithNoAliasMap;
    }

    public void addAnOldJson(String oldJson) {
        this.oldJsonFiles.add(oldJson);
    }

    public void addARenamedFieldWithNoAlias(String keyValuePair) {
        //renamedFieldWithNoAliasCommaSeperated list format: latestName=oldName
        String key = keyValuePair.split("=")[0];
        String value = keyValuePair.split("=")[1];
        this.renamedFieldsWithNoAliasMap.put(key,value);
    }



    public void matchToSchema() throws IOException, SchemaConverterException {

        ConfigProp configProp = ConfigProp.getInstance();
        String inputDir = System.getProperty("user.dir")+configProp.getProperty("input.dir");
        String outputDir = System.getProperty("user.dir") + configProp.getProperty("output.dir");

        SchemaObject oldSchema = new SchemaObject(inputDir + "avsc/" + oldSchemaFile);
        SchemaObject latestSchema = new SchemaObject(inputDir + "avsc/" + latestSchemaFile);

        for (String oldJsonFile : oldJsonFiles) {

            //run BFS
            conditionSchemaUsingBFS(oldSchema,latestSchema,oldJsonFile,renamedFieldsWithNoAliasMap,inputDir);

            //read old json into a record object
            GenericData.Record record = RwUtils.readDetailedJson(latestSchema.getSchema(), oldSchema.getSchema(), oldSchema.getJson());// read in the input to record object
            System.out.println("\nJSON record in old schema:\n"+ oldSchema.getJson());
            System.out.println("\nJSON record in new schema:\n"+record);

            //write new json into avro file
            RwUtils.writeJson(outputDir+"json/"+oldJsonFile,record.toString());
            RwUtils.writeAvro(outputDir+"avro/"+oldJsonFile.substring(0,oldJsonFile.indexOf('.'))+".avro", latestSchema.getSchema(), record);// write record object into .avro file
        }

    }

    private void conditionSchemaUsingBFS(SchemaObject oldSchema, SchemaObject latestSchema, String oldJsonFile, HashMap<String,String> renamedWithNoAliasMap, String inputDir) throws IOException, SchemaConverterException {

        oldSchema.setJson(GeneralUtils.getJsonStringFromFile(inputDir + "json/" + oldJsonFile));

        BfsConditioner schemaConditioner = BfsConditioner.getInstance(oldSchema, latestSchema, oldSchema.getJson(), renamedWithNoAliasMap);//get singleton instance of SchemaConditioner

        schemaConditioner.startConversion();// this is the method to start the breadth-first process

        Schema.Parser parser = new Schema.Parser();//create new parser
        oldSchema.setSchema(parser.parse(schemaConditioner.getOldSchema().toString()));//set updated old schema
        oldSchema.setJson(schemaConditioner.getOldJson().toString());//set updated old json that has a modified structure if there were array wrappings/unwrappings in the schemas
        parser = new Schema.Parser();//reset the parser
        latestSchema.setSchema(parser.parse(schemaConditioner.getLatestSchema().toString()));//set updated latest schema

        System.out.println("\nDEBUG OLD:\n"+ oldSchema.getSchema());
        System.out.println("DEBUG latest:\n"+ latestSchema.getSchema());

    }

}
