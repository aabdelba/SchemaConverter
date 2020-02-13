
package com.personal.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class GeneralUtils {

	public static String readPropertyFromSystem(String propertyName, Properties prop) {
		if ((System.getProperty(propertyName)) != null && !(System.getProperty(propertyName)).isEmpty()
				&& !(System.getProperty(propertyName)).trim().toLowerCase().equals("null"))
			return System.getProperty(propertyName);
		else
			return prop.getProperty(propertyName);
	}

	public static Set<String> getSetFromCommaSeperatedString(String renamedWithNoAlias) {

		renamedWithNoAlias = renamedWithNoAlias.replace("\n", "").replace("\r", "");

		ArrayList<String> listOfRenames = new ArrayList<>();
		for (String renamedField : renamedWithNoAlias.split(",")) {
			listOfRenames.add(renamedField.trim());
		}
		return new HashSet<>(listOfRenames);
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


}




