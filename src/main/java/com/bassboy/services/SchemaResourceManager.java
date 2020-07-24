package com.bassboy.services;

import com.bassboy.models.FormModel;
import com.bassboy.schemaevolver.InvalidSchemaEntryException;
import com.bassboy.schemaevolver.SchemaEvolver;
import com.bassboy.schemaevolver.SchemaEvolverException;
import com.bassboy.common.RwUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//@Service
// if this is annotated with @Service, must remove its @Bean method in configuration class as
// @Service will autocreate this bean
public class SchemaResourceManager {

    private final SchemaEvolver schemaEvolver;
    private String inputRecordDir;
    private String inputSchemaDir;
    private String outputJsonDir;
    private String outputAvroDir;
    private String outputDir;
    private String demoDir;
    private String downloadFormat;
    private boolean completeWithError = true; //always true, unless init() is called

    public boolean isCompleteWithError() {
        return completeWithError;
    }

    public String getDownloadFormat() {
        return downloadFormat;
    }

    public void setDownloadFormat(String downloadFormat) {
        this.downloadFormat = downloadFormat;
    }

    public SchemaEvolver getSchemaEvolver() {
        return schemaEvolver;
    }

    public SchemaResourceManager(SchemaEvolver schemaEvolver) {
        this.schemaEvolver = schemaEvolver;
    }

    public void init() throws IOException {
        System.out.println("Initializing resources...");
        setDirectories();
        clearDirectories();
        completeWithError=false;
    }

    public void setDirectories() {
        String ioDir = schemaEvolver.getIoDir();
        if(ioDir.charAt(0)!='/' && !ioDir.contains(System.getProperty("user.dir")))
            //defensive coding - if relative path provided, assume it is relative to project root
            ioDir = System.getProperty("user.dir")+"/"+ioDir;
        System.out.println("SchemaEvolver IO directory: "+ioDir);//absolute path
        this.inputRecordDir=ioDir + "/input/record/";;
        this.inputSchemaDir=ioDir + "/input/schema/";
        this.outputJsonDir=ioDir + "/output/json/";
        this.outputAvroDir=ioDir + "/output/avro/";
        this.outputDir=ioDir + "/output/";
        this.demoDir=ioDir + "/demo/";
    }

    private void clearDirectories() throws IOException {
        RwUtils.clearDirectory(inputRecordDir);
        RwUtils.clearDirectory(inputSchemaDir);
        RwUtils.clearDirectory(outputJsonDir);
        RwUtils.clearDirectory(outputAvroDir);
    }

    public void writeMultifileToInputDirectories(FormModel formModel) throws IOException {

        System.out.println("Writing uploaded files to "+inputSchemaDir);
        RwUtils.writeMultipartIntoFile(inputSchemaDir, formModel.getOldSchemaFile());
        RwUtils.writeMultipartIntoFile(inputSchemaDir, formModel.getNewSchemaFile());
        RwUtils.writeMultipartIntoFile(inputSchemaDir, formModel.getRenamedFile());

        if(formModel.getOldJsonFiles()!=null)
            for (MultipartFile record: formModel.getOldJsonFiles()) {
                RwUtils.writeMultipartIntoFile(inputRecordDir,record);
            }
    }

    public void writeTextboxToInputDirectories(FormModel formModel) throws IOException {

        System.out.println("Writing textbox data into files in "+inputSchemaDir);
        if(formModel.getOldSchemaFile()==null || formModel.getOldSchemaFile().isEmpty()) RwUtils.writeStringToFile(inputSchemaDir+"oldSchema.avsc", formModel.getOldSchemaText());
        if(formModel.getNewSchemaFile()==null || formModel.getNewSchemaFile().isEmpty()) RwUtils.writeStringToFile(inputSchemaDir+"newSchema.avsc", formModel.getNewSchemaText());
        if(formModel.getRenamedFile()==null || formModel.getRenamedFile().isEmpty()) RwUtils.writeStringToFile(inputSchemaDir+"renamedFields.txt", formModel.getRenamedText());

        int i = 1;
        String recordFileStr;
        for (String record: formModel.getOldJsonText().split(";;;")) {
            recordFileStr = inputRecordDir+"textboxRecord"+i+".json";
            RwUtils.writeStringToFile(recordFileStr,record);
            i++;
        }
    }

    public void runConversion(FormModel formModel) throws IOException, SchemaEvolverException, InvalidSchemaEntryException {
        init();
        writeTextboxToInputDirectories(formModel);
        writeMultifileToInputDirectories(formModel);
        setDownloadFormat(formModel.getDownloadFormat());
        String oldSchemaName;
        String newSchemaName;
        String renamedFileName;
        if(formModel.getOldSchemaFile().isEmpty()) oldSchemaName = "oldSchema.avsc"; else oldSchemaName = formModel.getOldSchemaFile().getOriginalFilename();
        if(formModel.getNewSchemaFile().isEmpty()) newSchemaName = "newSchema.avsc"; else newSchemaName  = formModel.getNewSchemaFile().getOriginalFilename();
        if(formModel.getRenamedFile().isEmpty()) renamedFileName = "renamedFields.txt"; else renamedFileName = formModel.getRenamedFile().getOriginalFilename();

        File recordDir = new File(inputRecordDir);
        String oldSchemaFile = inputSchemaDir + oldSchemaName;
        String newSchemaFile = inputSchemaDir + newSchemaName;
        String renamedFile = inputSchemaDir + renamedFileName;

        for (File oldJsonFile:recordDir.listFiles()) {
            try {
                schemaEvolver.convertDataAndPlaceInOutputDir(oldSchemaFile, newSchemaFile, oldJsonFile.getAbsolutePath(), renamedFile, outputDir);
            } catch (Exception e){
                completeWithError = true;
                e.printStackTrace();
                RwUtils.writeStringToFile(outputJsonDir+"ERROR_"+oldJsonFile.getName(),e.getMessage());
                RwUtils.writeStringToFile(outputAvroDir+"ERROR_"+oldJsonFile.getName().substring(0,oldJsonFile.getName().indexOf('.'))+".avro",e.getMessage());
            }
        }
    }

    public void download(ZipOutputStream zippedOut) throws IOException {
        FileSystemResource resource;
        ZipEntry zipEntry;
        File dir;
        setDirectories();
        String downloadDir;

        for (String format:downloadFormat.split("-")) {
            if(format.equals("demo"))
                downloadDir = demoDir;
            else
                downloadDir = outputDir+format;
            dir = new File(downloadDir);
            for (File file:dir.listFiles()) {
                resource = new FileSystemResource(downloadDir+"/"+file.getName());
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

        FormModel formModel = new FormModel(oldJsonFiles,oldSchemaFile,newSchemaFile,renamedFile,
                oldJsonText,oldSchemaText,newSchemaText,renamedText,downloadFormat);

//        SchemaResourceManager resourceManager = new SchemaResourceManager();
//        resourceManager.setFormModel(formModel);
//        resourceManager.init();
    }

}
