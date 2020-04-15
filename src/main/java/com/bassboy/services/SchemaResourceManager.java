package com.bassboy.services;

import com.bassboy.models.SchemaConverterModel;
import com.bassboy.schemaconversion.SchemaConverterMain;
import com.bassboy.schemaconversion.SchemaConverterException;
import com.bassboy.utils.ConfigProp;
import com.bassboy.utils.RwUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


public class SchemaResourceManager {

    private ConfigProp configProp;
    private SchemaConverterModel scm;
    private String oldSchemaName;
    private String newSchemaName;
    private String renamedFileName;

    public String getOldSchemaName() {
        return oldSchemaName;
    }

    public String getNewSchemaName() {
        return newSchemaName;
    }

    public String getRenamedFileName() {
        return renamedFileName;
    }

    public void setOldSchemaName(String oldSchemaName) {
        this.oldSchemaName = oldSchemaName;
    }

    public void setNewSchemaName(String newSchemaName) {
        this.newSchemaName = newSchemaName;
    }

    public void setRenamedFileName(String renamedFileName) {
        this.renamedFileName = renamedFileName;
    }

    public SchemaResourceManager(SchemaConverterModel scm) {
        this.scm = scm;
    }

    public SchemaConverterModel getScm() {
        return scm;
    }

    public void init() throws IOException {
        System.out.println("Initializing resources...");
        clearDirectories();
        writeTextboxToInputDirectories();
        writeMultifileToInputDirectories();
    }

    private void clearDirectories() throws IOException {
        configProp = configProp.getInstance();
        RwUtils.clearDirectory(System.getProperty("user.dir")+configProp.getProperty("input.dir")+"record/");
        RwUtils.clearDirectory(System.getProperty("user.dir")+configProp.getProperty("input.dir")+"schema/");
        RwUtils.clearDirectory(System.getProperty("user.dir")+configProp.getProperty("output.dir")+"json/");
        RwUtils.clearDirectory(System.getProperty("user.dir")+configProp.getProperty("output.dir")+"avro/");
    }

    private void writeMultifileToInputDirectories() throws IOException {

        configProp = configProp.getInstance();
        String recordDir = System.getProperty("user.dir")+configProp.getProperty("input.dir")+"record/";
        String schemaDir = System.getProperty("user.dir")+configProp.getProperty("input.dir")+"schema/";

        MultipartFile oldSchemaMPF = scm.getOldSchemaFile();
        MultipartFile newSchemaMPF = scm.getNewSchemaFile();
        MultipartFile renamedMPF = scm.getRenamedFile();

        if(!oldSchemaMPF.isEmpty()) setOldSchemaName(oldSchemaMPF.getOriginalFilename());
        if(!newSchemaMPF.isEmpty()) setNewSchemaName(newSchemaMPF.getOriginalFilename());
        if(!renamedMPF.isEmpty()) setRenamedFileName(renamedMPF.getOriginalFilename());

        RwUtils.writeMultipartIntoFile(schemaDir,oldSchemaMPF);
        RwUtils.writeMultipartIntoFile(schemaDir,newSchemaMPF);
        RwUtils.writeMultipartIntoFile(schemaDir,renamedMPF);

        for (MultipartFile record: scm.getOldJsonFiles()) {
            RwUtils.writeMultipartIntoFile(recordDir,record);
        }
    }

    private void writeTextboxToInputDirectories() throws IOException {
        configProp = configProp.getInstance();
        String recordDir = System.getProperty("user.dir")+configProp.getProperty("input.dir")+"record/";
        String schemaDir = System.getProperty("user.dir")+configProp.getProperty("input.dir")+"schema/";

        if(scm.getOldSchemaFile().isEmpty()){
            RwUtils.writeStringToFile(schemaDir+"oldSchema.avsc",scm.getOldSchemaText());
            oldSchemaName = "oldSchema.avsc";
        }
        if(scm.getNewSchemaFile().isEmpty()){
            RwUtils.writeStringToFile(schemaDir+"newSchema.avsc",scm.getNewSchemaText());
            newSchemaName = "newSchema.avsc";
        }
        if(scm.getRenamedFile().isEmpty()) {
            RwUtils.writeStringToFile(schemaDir+"renamedFields.txt",scm.getRenamedText());
            renamedFileName = "renamedFields.txt";
        }

        int i = 1;
        String recordFileStr;
        for (String record:scm.getOldJsonText().split(";;;")) {
            recordFileStr = recordDir+"textboxRecord"+i+".json";
            RwUtils.writeStringToFile(recordFileStr,record);
            i++;
        }
    }

    public void runConversion() throws IOException, SchemaConverterException {
        configProp = ConfigProp.getInstance();
        String inputDir = System.getProperty("user.dir")+configProp.getProperty("input.dir");
        String outputDir = System.getProperty("user.dir")+configProp.getProperty("output.dir");

        File recordDir = new File(inputDir + "record/");
        File oldSchemaFile = new File(inputDir + "schema/" + getOldSchemaName());
        File newSchemaFile = new File(inputDir + "schema/" + getNewSchemaName());
        File renamedFile = new File(inputDir + "schema/" + getRenamedFileName());

        SchemaConverterMain sc = new SchemaConverterMain();

        for (File oldJsonFile:recordDir.listFiles()) {
            try {
                sc.matchToSchema(oldSchemaFile, newSchemaFile, oldJsonFile, renamedFile);
            } catch (Exception e){
                e.printStackTrace();
                RwUtils.writeStringToFile(outputDir+"json/ERROR_"+oldJsonFile.getName(),e.getMessage());
                RwUtils.writeStringToFile(outputDir+"avro/ERROR_"+oldJsonFile.getName().substring(0,oldJsonFile.getName().indexOf('.'))+".avro",e.getMessage());
            }
        }
    }

}
