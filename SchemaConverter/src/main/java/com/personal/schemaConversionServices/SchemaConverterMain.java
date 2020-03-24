
package com.personal.schemaConversionServices;

import com.personal.models.SchemaConverterModel;
import com.personal.utils.ConfigProp;
import com.personal.utils.GeneralUtils;

import java.util.*;

public class SchemaConverterMain {

	public static void main(String[] args) throws Exception {

		// TODO: this is all to be done in the controller
		ConfigProp configProp = ConfigProp.getInstance();
		String inputDir = configProp.getProperty("input.dir");
		String oldSchemaFile = "schema1.avsc";
		String latestSchemaFile = "schema2.avsc";
		List<String> oldJsonFiles = new ArrayList<>();
		oldJsonFiles.add("record.json");
		//renamedFieldWithNoAliasCommaSeperated list format: latestName=oldName
		String renamedFieldWithNoAliasDelimiterSeperated = "SchoolFriend=SchoolFriends\nemailAddress=email";
		HashMap<String,String> renamedWithNoAliasMap = GeneralUtils.getMapFromNewlineSeperatedString(renamedFieldWithNoAliasDelimiterSeperated);
		SchemaConverterModel schemaConverterModel = new SchemaConverterModel(oldSchemaFile,latestSchemaFile,oldJsonFiles,renamedWithNoAliasMap);

		// run the conversion
		schemaConverterModel.runConversion();
	}

}

