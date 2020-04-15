package com.bassboy.models;

import com.bassboy.schemaconversion.BfsConditioner;
import com.bassboy.schemaconversion.SchemaConverterException;
import com.bassboy.schemaconversion.SchemaObject;
import com.bassboy.utils.ConfigProp;
import com.bassboy.utils.RwUtils;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SchemaConverterModel {

    private ConfigProp configProp = ConfigProp.getInstance();
    private MultipartFile[] oldJsonFiles;
    private MultipartFile oldSchemaFile;
    private MultipartFile newSchemaFile;
    private MultipartFile renamedFile;
    private String oldJsonText;
    private String oldSchemaText;
    private String newSchemaText;
    private String renamedText;
    private String fileFormat;

    public SchemaConverterModel() throws IOException {
    }

    public SchemaConverterModel(MultipartFile[] oldJsonFiles, MultipartFile oldSchemaFile, MultipartFile newSchemaFile, MultipartFile renamedFile, String oldJsonText, String oldSchemaText, String newSchemaText, String renamedText, String fileFormat) throws IOException {
        this.oldJsonFiles = oldJsonFiles;
        this.oldSchemaFile = oldSchemaFile;
        this.newSchemaFile = newSchemaFile;
        this.renamedFile = renamedFile;
        this.oldJsonText = oldJsonText;
        this.oldSchemaText = oldSchemaText;
        this.newSchemaText = newSchemaText;
        this.renamedText = renamedText;
        this.fileFormat = fileFormat;
    }

    public MultipartFile[] getOldJsonFiles() {
        return oldJsonFiles;
    }

    public void setOldJsonFiles(MultipartFile[] oldJsonFiles) {
        this.oldJsonFiles = oldJsonFiles;
    }

    public MultipartFile getOldSchemaFile() {
        return oldSchemaFile;
    }

    public void setOldSchemaFile(MultipartFile oldSchemaFile) {
        this.oldSchemaFile = oldSchemaFile;
    }

    public MultipartFile getNewSchemaFile() {
        return newSchemaFile;
    }

    public void setNewSchemaFile(MultipartFile newSchemaFile) {
        this.newSchemaFile = newSchemaFile;
    }

    public MultipartFile getRenamedFile() {
        return renamedFile;
    }

    public void setRenamedFile(MultipartFile renamedFile) {
        this.renamedFile = renamedFile;
    }

    public String getOldJsonText() {
        return oldJsonText;
    }

    public void setOldJsonText(String oldJsonText) {
        this.oldJsonText = oldJsonText;
    }

    public String getOldSchemaText() {
        return oldSchemaText;
    }

    public void setOldSchemaText(String oldSchemaText) {
        this.oldSchemaText = oldSchemaText;
    }

    public String getNewSchemaText() {
        return newSchemaText;
    }

    public void setNewSchemaText(String newSchemaText) {
        this.newSchemaText = newSchemaText;
    }

    public String getRenamedText() {
        return renamedText;
    }

    public void setRenamedText(String renamedText) {
        this.renamedText = renamedText;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }
}
