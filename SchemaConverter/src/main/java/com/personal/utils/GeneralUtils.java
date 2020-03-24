
package com.personal.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public final class GeneralUtils {

	public static String readPropertyFromSystem(String propertyName, Properties prop) {
		if ((System.getProperty(propertyName)) != null && !(System.getProperty(propertyName)).isEmpty()
				&& !(System.getProperty(propertyName)).trim().toLowerCase().equals("null"))
			return System.getProperty(propertyName);
		else
			return prop.getProperty(propertyName);
	}

	public static HashMap<String,String> getMapFromNewlineSeperatedString(String renamedWithNoAlias) {

		renamedWithNoAlias = renamedWithNoAlias.replace("\r", "\n");

		HashMap<String,String> mapOfRenames = new HashMap<>();
		for (String keyValuePair : renamedWithNoAlias.split("\n")) {
			keyValuePair=keyValuePair.trim().replace(" ","");
			if(keyValuePair.length()>=3) {
				//renamedFieldWithNoAliasCommaSeperated list format: latestName=oldName
				String key = keyValuePair.split("=")[0];
				String value = keyValuePair.split("=")[1];
				mapOfRenames.put(key, value);
			}
		}
		return mapOfRenames;
	}

	public static String convertFileContentToString(String fileName) throws IOException {

		File file = new File(fileName);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line;
		StringBuilder sb = new StringBuilder();
		while((line = br.readLine()) != null){
			sb.append(line);			
		}
		return sb.toString();
	}


	public static String getJsonStringFromFile(String jsonFile) throws IOException {
		return GeneralUtils.convertFileContentToString(jsonFile)
				.replace("\n", "")
				.replace("\t", "")
				.replace("\r", "")
				.replace(" ", "");
	}
}




