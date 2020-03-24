package com.personal.models;

import com.personal.schemaConversionServices.SchemaConverterException;
import com.personal.schemaConversionServices.SchemaObject;
import com.personal.utils.RwUtils;
import com.personal.utils.ConfigProp;
import com.personal.utils.GeneralUtils;
import org.apache.avro.generic.GenericData;

import java.io.IOException;
import java.util.*;

public class SchemaConverterModel {

    private ConfigProp configProp = ConfigProp.getInstance();
    private String latestSchemaFile;
    private String oldSchemaFile;
    private List<java.lang.String> oldJsonFiles;
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

    public SchemaConverterModel(String latestSchemaFile, String oldSchemaFile, List<String> oldJsonFiles) throws IOException {
        this.latestSchemaFile = latestSchemaFile;
        this.oldSchemaFile = oldSchemaFile;
        this.oldJsonFiles = oldJsonFiles;
    }

    public SchemaConverterModel(String latestSchemaFile, String oldSchemaFile) throws IOException {
        this.latestSchemaFile = latestSchemaFile;
        this.oldSchemaFile = oldSchemaFile;
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

    public void runConversion() throws IOException, SchemaConverterException {

        ConfigProp configProp = ConfigProp.getInstance();
        String inputDir = System.getProperty("user.dir")+configProp.getProperty("input.dir");
        String outputDir = System.getProperty("user.dir") + configProp.getProperty("output.dir");

        SchemaObject oldSchema = new SchemaObject(inputDir + "avsc/" + oldSchemaFile);
        SchemaObject latestSchema = new SchemaObject(inputDir + "avsc/" + latestSchemaFile);

        for (String oldJsonFile : oldJsonFiles) {
            //match schemas
            oldSchema.setJson(GeneralUtils.getJsonStringFromFile(inputDir + "json/" + oldJsonFile));
            oldSchema.matchToSchema(latestSchema,renamedFieldsWithNoAliasMap);// add new schema entities to the old schema. Only do if they are non compliant to schema evolution standards
    
            System.out.println("\nDEBUG OLD:\n"+ oldSchema.getSchema());
            System.out.println("DEBUG latest:\n"+ latestSchema.getSchema());
    
            //read old json into a record object
            GenericData.Record record = RwUtils.readDetailedJson(latestSchema.getSchema(), oldSchema.getSchema(), oldSchema.getJson());// read in the input to record object
            System.out.println("\nJSON record in old schema:\n"+ oldSchema.getJson());
            System.out.println("\nJSON record in new schema:\n"+record);
    
            //write new json into avro file
            RwUtils.writeJson(outputDir+"json/"+oldJsonFile,record.toString());
            RwUtils.writeAvro(outputDir+"avro/"+oldJsonFile.substring(0,oldJsonFile.indexOf('.'))+".avro", latestSchema.getSchema(), record);// write record object into .avro file
        }
    }

}
