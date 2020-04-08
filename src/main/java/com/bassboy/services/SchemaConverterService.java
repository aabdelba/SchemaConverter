
package com.bassboy.services;

import com.bassboy.schemaConversion.SchemaConverterException;
import com.bassboy.schemaConversion.SchemaObject;
import com.bassboy.utils.ConfigProp;
import com.bassboy.utils.GeneralUtils;
import com.bassboy.utils.RwUtils;
import org.apache.avro.generic.GenericData;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class SchemaConverterService {

	public static void main(String[] args) throws Exception {
		// this is all to be done in the controller
		ConfigProp configProp = ConfigProp.getInstance();
		String inputDir = configProp.getProperty("input.dir");





//		String oldSchemaFile = "schema1.avsc";
//		String latestSchemaFile = "schema2.avsc";
//		List<String> oldJsonFiles = new ArrayList<>();
//		oldJsonFiles.add("record.json");
		// renamedFieldWithNoAliasCommaSeperated list format: latestName=oldName
//		String renamedFieldWithNoAliasDelimiterSeperated = "SchoolFriend=SchoolFriends\nemailAddress=email";

		String oldSchemaFile = "demo1.avsc";
		String latestSchemaFile = "demo2.avsc";
		List<String> oldJsonFiles = new ArrayList<>();
		oldJsonFiles.add("demo.json");
		// renamedFieldWithNoAliasCommaSeperated list format: latestName=oldName
		String renamedFieldWithNoAliasDelimiterSeperated = "refId=id\nfName=firstName\npersonAge=age";







		HashMap<String, String> renamedWithNoAliasMap = GeneralUtils.getMapFromNewlineSeperatedString(renamedFieldWithNoAliasDelimiterSeperated);
		runConversion(oldSchemaFile,latestSchemaFile,oldJsonFiles,renamedWithNoAliasMap);
	}

	public static void runConversion(String oldSchemaFile, String latestSchemaFile, List<String> oldJsonFiles, HashMap<String,String> renamedWithNoAliasMap) throws IOException, SchemaConverterException {
		// run the conversion
		ConfigProp configProp = ConfigProp.getInstance();
		String inputDir = System.getProperty("user.dir")+configProp.getProperty("input.dir");
		String outputDir = System.getProperty("user.dir") + configProp.getProperty("output.dir");

		SchemaObject oldSchema = new SchemaObject(inputDir + "avsc/" + oldSchemaFile);
		SchemaObject latestSchema = new SchemaObject(inputDir + "avsc/" + latestSchemaFile);

		for (String oldJsonFile : oldJsonFiles) {
			//match schemas
			oldSchema.setJson(GeneralUtils.getJsonStringFromFile(inputDir + "json/" + oldJsonFile));
			oldSchema.matchToSchema(latestSchema,renamedWithNoAliasMap);// add new schema entities to the old schema. Only do if they are non compliant to schema evolution standards

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

