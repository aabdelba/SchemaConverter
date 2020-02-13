
package com.personal.schemaconversion;

import java.util.Set;

import org.apache.avro.generic.GenericData.Record;

import com.personal.utils.AvroRW;
import com.personal.utils.GeneralUtils;

public class SchemaConverterMain {

	static String resourcesDir = System.getProperty("user.dir") + "/src/main/resources";

	public static void main(String[] args) throws Exception {
		
		SchemaObject latestSchema = new SchemaObject(resourcesDir + "/schema2.avsc");
		SchemaObject oldSchema = new SchemaObject(resourcesDir + "/schema1.avsc");
		String json = GeneralUtils.convertFileContentToString(resourcesDir + "/record.json").replace("\n", "").replace("\t", "").replace("\r", "").replace(" ", "");
		
		//match schemas
		//renamedWithNoAlias list format: latestName=oldName
		String renamedWithNoAlias = "SchoolFriend=SchoolFriends,emailAddress=email";
		Set<String> renamedFields = GeneralUtils.getSetFromCommaSeperatedString(renamedWithNoAlias);
		oldSchema.setJson(json);
		oldSchema.matchToSchema(latestSchema,renamedFields);// add new schema entities to the old schema. Only do if they are non compliant to schema evolution standards		
		
		System.out.println("\nDEBUG OLD:\n"+oldSchema.getSchema());
		System.out.println("DEBUG latest:\n"+latestSchema.getSchema());
		
		//read old json into a record object
		Record record = AvroRW.readDetailedJson(latestSchema.getSchema(), oldSchema.getSchema(), oldSchema.getJson());// read in the input to record object
		System.out.println("\nJSON record in old schema:\n"+oldSchema.getJson());
		System.out.println("\nJSON record in new schema:\n"+record);
		
		//write new json into avro file
		AvroRW.writeAvro(latestSchema.getSchema(), record);// write record object into .avro file
		
	}

}

