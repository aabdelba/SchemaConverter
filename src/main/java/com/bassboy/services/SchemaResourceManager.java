package com.bassboy.services;

import com.bassboy.models.FormModel;
import com.bassboy.schemaevolver.InvalidEntryException;
import com.bassboy.schemaevolver.SchemaEvolverMain;
import com.bassboy.schemaevolver.SchemaEvolverException;
import com.bassboy.common.ConfigProp;
import com.bassboy.common.RwUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class SchemaResourceManager {

    private ConfigProp configProp;

    @Autowired
    private FormModel scm;

    public SchemaResourceManager(FormModel scm) {
        this.scm = scm;
    }

    public FormModel getScm() {
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

        RwUtils.writeMultipartIntoFile(schemaDir,scm.getOldSchemaFile());
        RwUtils.writeMultipartIntoFile(schemaDir,scm.getNewSchemaFile());
        RwUtils.writeMultipartIntoFile(schemaDir,scm.getRenamedFile());

        for (MultipartFile record: scm.getOldJsonFiles()) {
            RwUtils.writeMultipartIntoFile(recordDir,record);
        }
    }

    private void writeTextboxToInputDirectories() throws IOException {
        configProp = configProp.getInstance();
        String recordDir = System.getProperty("user.dir")+configProp.getProperty("input.dir")+"record/";
        String schemaDir = System.getProperty("user.dir")+configProp.getProperty("input.dir")+"schema/";

        if(scm.getOldSchemaFile().isEmpty()) RwUtils.writeStringToFile(schemaDir+"oldSchema.avsc",scm.getOldSchemaText());
        if(scm.getNewSchemaFile().isEmpty()) RwUtils.writeStringToFile(schemaDir+"newSchema.avsc",scm.getNewSchemaText());
        if(scm.getRenamedFile().isEmpty()) RwUtils.writeStringToFile(schemaDir+"renamedFields.txt",scm.getRenamedText());

        int i = 1;
        String recordFileStr;
        for (String record:scm.getOldJsonText().split(";;;")) {
            recordFileStr = recordDir+"textboxRecord"+i+".json";
            RwUtils.writeStringToFile(recordFileStr,record);
            i++;
        }
    }

    public void runConversion() throws IOException, SchemaEvolverException, InvalidEntryException {
        configProp = ConfigProp.getInstance();
        String inputDir = System.getProperty("user.dir")+configProp.getProperty("input.dir");
        String outputDir = System.getProperty("user.dir")+configProp.getProperty("output.dir");

        String oldSchemaName;
        String newSchemaName;
        String renamedFileName;
        if(scm.getOldSchemaFile().isEmpty()) oldSchemaName = "oldSchema.avsc"; else oldSchemaName = scm.getOldSchemaFile().getOriginalFilename();
        if(scm.getNewSchemaFile().isEmpty()) newSchemaName = "newSchema.avsc"; else newSchemaName  = scm.getNewSchemaFile().getOriginalFilename();
        if(scm.getRenamedFile().isEmpty()) renamedFileName = "renamedFields.txt"; else renamedFileName = scm.getRenamedFile().getOriginalFilename();

        File recordDir = new File(inputDir + "record/");
        File oldSchemaFile = new File(inputDir + "schema/" + oldSchemaName);
        File newSchemaFile = new File(inputDir + "schema/" + newSchemaName);
        File renamedFile = new File(inputDir + "schema/" + renamedFileName);

        SchemaEvolverMain sc = new SchemaEvolverMain();

        for (File oldJsonFile:recordDir.listFiles()) {
            try {
                sc.convertDataAndPlaceInOutputDir(oldSchemaFile, newSchemaFile, oldJsonFile, renamedFile);
            } catch (Exception e){
                e.printStackTrace();
                RwUtils.writeStringToFile(outputDir+"json/ERROR_"+oldJsonFile.getName(),e.getMessage());
                RwUtils.writeStringToFile(outputDir+"avro/ERROR_"+oldJsonFile.getName().substring(0,oldJsonFile.getName().indexOf('.'))+".avro",e.getMessage());
            }
        }
    }

    public void download(ZipOutputStream zippedOut) throws IOException {
        ConfigProp configProp = ConfigProp.getInstance();
        String downloadFormat = getScm().getDownloadFormat();
        FileSystemResource resource;
        ZipEntry zipEntry;
        File dir;

        for (String dirStr:downloadFormat.split("-")) {
            dir = new File(System.getProperty("user.dir")+configProp.getProperty("output.dir")+dirStr);
            for (File file:dir.listFiles()) {
                resource = new FileSystemResource(System.getProperty("user.dir")+configProp.getProperty("output.dir")+dirStr+"/"+file.getName());

                zipEntry = new ZipEntry(file.getName());
                // Configure the zip entry, the properties of the file
                zipEntry.setSize(resource.contentLength());
                zipEntry.setTime(System.currentTimeMillis());
                // etc.
                zippedOut.putNextEntry(zipEntry);
                // And the content of the resource:
                StreamUtils.copy(resource.getInputStream(), zippedOut);
                zippedOut.closeEntry();
            }
        }
        zippedOut.finish();
    }


    public static void main(String[] args) throws IOException {

        MultipartFile[] oldJsonFiles = new MultipartFile[0];
        MultipartFile oldSchemaFile = null;
        MultipartFile newSchemaFile = null;
        MultipartFile renamedFile = null;

        String oldJsonText = "asdfasfad ;;; asdfwefer ;;; awfasdfver ;;;";
        String oldSchemaText = "asdfasdfawefwerf";
        String newSchemaText = "asdfasdfawefwerf";
        String renamedText = "asdfasdfawefwerf=adfwedcf";

        String downloadFormat = "json";

        FormModel scv = new FormModel(oldJsonFiles,oldSchemaFile,newSchemaFile,renamedFile,
                oldJsonText,oldSchemaText,newSchemaText,renamedText,downloadFormat);

        SchemaResourceManager srm = new SchemaResourceManager(scv);
        srm.init();
    }

}
