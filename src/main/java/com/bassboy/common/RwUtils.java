package com.bassboy.common;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Properties;
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

	public static byte[] convertFileContentToBytes(String fullFilePath) throws IOException {
		File file = new File(fullFilePath);
		return convertFileContentToBytes(file);
	}

	public static byte[] convertFileContentToBytes(File file) throws IOException {
		return Files.readAllBytes(file.toPath());
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
		br.close();
		fr.close();
		return sb.toString();
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
				File destFile = createFileWithNewNameIfNotUnique(inputPath,mpf.getOriginalFilename());
				mpf.transferTo(destFile);
				System.out.println("Created " + destFile);
			}
		}
	}

	public static File createFileWithNewNameIfNotUnique(String inputPath, String originalFileName) {
		File destFile = new File(inputPath + originalFileName);
		if(destFile.exists()){
			String fileNameWithoutExtension = originalFileName.substring(0,originalFileName.lastIndexOf('.'));
			String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'),originalFileName.length());
			fileNameWithoutExtension = fileNameWithoutExtension + "_renamed";
			destFile = createFileWithNewNameIfNotUnique(inputPath,fileNameWithoutExtension+fileExtension);
		}
		return destFile;
	}

	public static void clearDirectory(String dirString) {
		File dir = new File(dirString);//.listFiles() throws null pointer when directory is empty
		for (File file : dir.listFiles()) {
			if(!file.delete())
			System.out.println("Deleted " + file.getAbsolutePath());
		}
	}

}
