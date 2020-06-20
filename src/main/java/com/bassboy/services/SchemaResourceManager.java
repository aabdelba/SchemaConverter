package com.bassboy.services;

import com.bassboy.models.FormModel;
import com.bassboy.schemaevolver.InvalidSchemaEntryException;
import com.bassboy.schemaevolver.SchemaEvolverMain;
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
public class SchemaResourceManager {

    private static SchemaResourceManager resourceManager;

    private String ioDir;
    private String inputRecordDir;
    private String inputSchemaDir;
    private String outputJsonDir;
    private String outputAvroDir;
    private String outputDir;
    private String demoDir;

    private boolean completeWithError = true; //always true, unless init() is called
    private FormModel formModel;


    public static SchemaResourceManager getInstance(String ioDir) {
        if (resourceManager == null)
            resourceManager = new SchemaResourceManager(ioDir);
        resourceManager.setIoDir(ioDir);
        return resourceManager;
    }

    public boolean isCompleteWithError() {
        return completeWithError;
    }

    public void setFormModel(FormModel formModel) {
        this.formModel = formModel;
    }

    private SchemaResourceManager(String ioDir) {
        this.ioDir = ioDir;
    }

    private SchemaResourceManager() {}

    public FormModel getFormModel() {
        return formModel;
    }

    public void setIoDir(String ioDir) {
        this.ioDir = ioDir;
    }

    public void init() throws IOException {
        System.out.println("Initializing resources...");
        clearDirectories();
        completeWithError=false;
        writeTextboxToInputDirectories();
        writeMultifileToInputDirectories();
    }

    public void setDirectories() {
        if(ioDir.charAt(0)!='/' && !ioDir.contains(System.getProperty("user.dir")))
            //defensive coding - if relative path provided, assume it is relative to project root
            ioDir = System.getProperty("user.dir")+"/"+ioDir;
        System.out.println("SchemaEvolver IO directory: "+ioDir);//absolute path
        this.inputRecordDir=ioDir + "/input/record/";;
        this.inputSchemaDir=ioDir + "/input/schema/";
        this.outputJsonDir=ioDir + "/output/json/";
        this.outputAvroDir=ioDir + "/output/avro/";
        this.outputDir=ioDir + "/output/";
        this.demoDir=System.getProperty("user.dir")+"/src/main/resources/demo";
    }

    private void clearDirectories() throws IOException {
        RwUtils.clearDirectory(inputRecordDir);
        RwUtils.clearDirectory(inputSchemaDir);
        RwUtils.clearDirectory(outputJsonDir);
        RwUtils.clearDirectory(outputAvroDir);
    }

    private void writeMultifileToInputDirectories() throws IOException {

        RwUtils.writeMultipartIntoFile(inputSchemaDir, formModel.getOldSchemaFile());
        RwUtils.writeMultipartIntoFile(inputSchemaDir, formModel.getNewSchemaFile());
        RwUtils.writeMultipartIntoFile(inputSchemaDir, formModel.getRenamedFile());

        for (MultipartFile record: formModel.getOldJsonFiles()) {
            RwUtils.writeMultipartIntoFile(inputRecordDir,record);
        }
    }

    private void writeTextboxToInputDirectories() throws IOException {

        if(formModel.getOldSchemaFile().isEmpty()) RwUtils.writeStringToFile(inputSchemaDir+"oldSchema.avsc", formModel.getOldSchemaText());
        if(formModel.getNewSchemaFile().isEmpty()) RwUtils.writeStringToFile(inputSchemaDir+"newSchema.avsc", formModel.getNewSchemaText());
        if(formModel.getRenamedFile().isEmpty()) RwUtils.writeStringToFile(inputSchemaDir+"renamedFields.txt", formModel.getRenamedText());

        int i = 1;
        String recordFileStr;
        for (String record: formModel.getOldJsonText().split(";;;")) {
            recordFileStr = inputRecordDir+"textboxRecord"+i+".json";
            RwUtils.writeStringToFile(recordFileStr,record);
            i++;
        }
    }

    public void runConversion() throws IOException, SchemaEvolverException, InvalidSchemaEntryException {

        String oldSchemaName;
        String newSchemaName;
        String renamedFileName;
        if(formModel.getOldSchemaFile().isEmpty()) oldSchemaName = "oldSchema.avsc"; else oldSchemaName = formModel.getOldSchemaFile().getOriginalFilename();
        if(formModel.getNewSchemaFile().isEmpty()) newSchemaName = "newSchema.avsc"; else newSchemaName  = formModel.getNewSchemaFile().getOriginalFilename();
        if(formModel.getRenamedFile().isEmpty()) renamedFileName = "renamedFields.txt"; else renamedFileName = formModel.getRenamedFile().getOriginalFilename();

        File recordDir = new File(inputRecordDir);
        File oldSchemaFile = new File(inputSchemaDir + oldSchemaName);
        File newSchemaFile = new File(inputSchemaDir + newSchemaName);
        File renamedFile = new File(inputSchemaDir + renamedFileName);

        SchemaEvolverMain schemaEvolver = new SchemaEvolverMain();

        for (File oldJsonFile:recordDir.listFiles()) {
            try {
                schemaEvolver.convertDataAndPlaceInOutputDir(oldSchemaFile, newSchemaFile, oldJsonFile, renamedFile, outputDir);
            } catch (Exception e){
                completeWithError = true;
                e.printStackTrace();
                RwUtils.writeStringToFile(outputJsonDir+"ERROR_"+oldJsonFile.getName(),e.getMessage());
                RwUtils.writeStringToFile(outputAvroDir+"ERROR_"+oldJsonFile.getName().substring(0,oldJsonFile.getName().indexOf('.'))+".avro",e.getMessage());
            }
        }
    }

    public void download(ZipOutputStream zippedOut) throws IOException {
        String downloadFormat = getFormModel().getDownloadFormat();
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

//        SchemaResourceManager resourceManager = new SchemaResourceManager(formModel);
        resourceManager.init();
    }

}
