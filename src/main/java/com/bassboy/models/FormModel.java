package com.bassboy.models;

import com.bassboy.common.ConfigProp;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class FormModel {

    private MultipartFile[] oldJsonFiles;
    private MultipartFile oldSchemaFile;
    private MultipartFile newSchemaFile;
    private MultipartFile renamedFile;
    private String oldJsonText;
    private String oldSchemaText;
    private String newSchemaText;
    private String renamedText;
    private String downloadFormat;

    @Override
    public String toString() {
        return "SchemaConverterModel{" +
                "oldJsonFiles=" + Arrays.toString(oldJsonFiles) +
                ", oldSchemaFile=" + oldSchemaFile +
                ", newSchemaFile=" + newSchemaFile +
                ", renamedFile=" + renamedFile +
                ", oldJsonText='" + oldJsonText + '\'' +
                ", oldSchemaText='" + oldSchemaText + '\'' +
                ", newSchemaText='" + newSchemaText + '\'' +
                ", renamedText='" + renamedText + '\'' +
                ", fileFormat='" + downloadFormat + '\'' +
                '}';
    }

    public FormModel() throws IOException {
    }

    public FormModel(MultipartFile[] oldJsonFiles, MultipartFile oldSchemaFile, MultipartFile newSchemaFile, MultipartFile renamedFile, String oldJsonText, String oldSchemaText, String newSchemaText, String renamedText, String fileFormat) throws IOException {
        this.oldJsonFiles = oldJsonFiles;
        this.oldSchemaFile = oldSchemaFile;
        this.newSchemaFile = newSchemaFile;
        this.renamedFile = renamedFile;
        this.oldJsonText = oldJsonText;
        this.oldSchemaText = oldSchemaText;
        this.newSchemaText = newSchemaText;
        this.renamedText = renamedText;
        this.downloadFormat = fileFormat;
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

    public String getDownloadFormat() {
        return downloadFormat;
    }

    public void setDownloadFormat(String downloadFormat) {
        this.downloadFormat = downloadFormat;
    }
}
