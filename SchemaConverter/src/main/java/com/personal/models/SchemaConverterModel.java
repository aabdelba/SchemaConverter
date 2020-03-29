package com.personal.models;

import com.personal.utils.ConfigProp;

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

}
