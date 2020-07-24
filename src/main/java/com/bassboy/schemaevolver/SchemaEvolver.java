package com.bassboy.schemaevolver;

import java.io.IOException;

public interface SchemaEvolver {
    public void convertDataAndPlaceInOutputDir(String oldSchemaFile, String newSchemaFile, String oldJsonFile, String renamedFile, String outputDir) throws IOException, SchemaEvolverException, InvalidSchemaEntryException;
    String getIoDir();
}
