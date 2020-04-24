package com.bassboy.common;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

public final class RwUtils {

	public static String readPropertyFromSystem(String propertyName, Properties prop) {
		if ((System.getProperty(propertyName)) != null && !(System.getProperty(propertyName)).isEmpty()
				&& !(System.getProperty(propertyName)).trim().toLowerCase().equals("null"))
			return System.getProperty(propertyName);
		else
			return prop.getProperty(propertyName);
	}

	public static HashMap<String,String> getMapFromEqualSignNewlineSeparatedFile(File file) throws IOException {
		String renamedWithNoAlias = convertFileContentToString(file);
		renamedWithNoAlias = renamedWithNoAlias.replace("\r","")
											   .replace("\n","")
										       .replace("\t","")
											   .replace(" ","");

		HashMap<String,String> mapOfRenames = new HashMap<>();
		for (String keyValuePair : renamedWithNoAlias.split(";")) {
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

	public static String convertFileContentToString(String fullFilePath) throws IOException {
		File file = new File(fullFilePath);
		return convertFileContentToString(file);
	}

	public static String convertFileContentToString(File file) throws IOException {

		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line;
		StringBuilder sb = new StringBuilder();
		while((line = br.readLine()) != null){
			sb.append(line);
		}
		return sb.toString();
	}

	public static boolean isValidJson(String oldJsonString) {
		try{new JSONObject(oldJsonString);return true;}catch (Exception e){return false;}
	}

	public static void writeStringToFile(String fileStr, String string) throws IOException {
		if(!string.trim().replace("\n","").replace("\t","").replace("\n","")
				.equals("")) {
			Writer writer = new FileWriter(fileStr);
			writer.write(string);
			System.out.println("Created " + fileStr);
			writer.close();
		}
	}

	public static void writeMultipartIntoFile(String inputPath, MultipartFile mpf) throws IOException {
		if(mpf!=null){
			if(!mpf.isEmpty()) {
				mpf.transferTo(new File(inputPath + mpf.getOriginalFilename()));
				System.out.println("Created " + inputPath + mpf.getOriginalFilename());
			}
		}
	}

	public static void clearDirectory(String dirString) {
		File dir = new File(dirString);
		for (File file:dir.listFiles()) {
			file.delete();
			System.out.println("Deleted " + file.getAbsolutePath());
		};
	}

}
