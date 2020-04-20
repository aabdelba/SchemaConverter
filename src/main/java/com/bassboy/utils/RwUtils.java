package com.bassboy.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

import com.bassboy.schemaevolver.InvalidEntryException;
import com.bassboy.schemaevolver.SchemaEvolverException;
import com.bassboy.schemaevolver.SchemaObject;
import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.*;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.io.*;

import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;
import tech.allegro.schema.json2avro.converter.JsonGenericRecordReader;

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

	public static byte[] writeAvroToFile(String fullFilePath, Schema writerSchema, Object datum) throws IOException {
		GenericDatumWriter<Object> writer = new GenericDatumWriter<Object>(writerSchema);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Encoder e = EncoderFactory.get().binaryEncoder(outputStream, null);
		writer.write(datum, e);
		e.flush();

		// write to a file
		DataFileWriter<Object> dataFileWriter = new DataFileWriter<>(writer);
		dataFileWriter.create(writerSchema, new File(fullFilePath));
		dataFileWriter.append(datum);
		System.out.println("Created " + fullFilePath);
		dataFileWriter.close();
		return outputStream.toByteArray();
	}

	public static void main(String[] args) throws InvalidEntryException, IOException, SchemaEvolverException {
		ConfigProp configProp = ConfigProp.getInstance();
		String inputPath=System.getProperty("user.dir")+configProp.getProperty("debug.dir");
		SchemaObject oldSchema = new SchemaObject(inputPath+"schema/schema1.avsc");
		oldSchema.setJson(new File(inputPath+"record/record.avro"));

	}


	public static Record readUndetailedJson(Schema newSchema, Schema oldSchema, String json) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
		GenericDatumWriter<Object> writer = new GenericDatumWriter<>(oldSchema);
		
		writer.write(convertToGenericDataRecord(json.getBytes(), oldSchema), encoder);
		encoder.flush();
		return readRecord(newSchema,oldSchema,outputStream.toByteArray());
	}


	public static Record readDetailedJson(Schema newSchema, Schema oldSchema, String json) throws IOException {
		if (json == null) {
			return null;
		}
		GenericDatumReader<Record> reader = new GenericDatumReader<>();
		reader.setExpected(newSchema);
		reader.setSchema(oldSchema);

		Decoder decoder = DecoderFactory.get().jsonDecoder(oldSchema, json);

		try {
			return reader.read(null, decoder);
		} catch (AvroTypeException e) {
			throw e;
		}
	}

	public static Record readDetailedJson(Schema schema, String json) throws IOException {
		if (json == null) {
			return null;
		}
		GenericDatumReader<Record> reader = new GenericDatumReader<>();
		reader.setSchema(schema);

		Decoder decoder = DecoderFactory.get().jsonDecoder(schema, json);

		try {
			return reader.read(null, decoder);
		} catch (AvroTypeException e) {
			throw e;
		}
	}

	public static Object convertToGenericDataRecord(byte[] data, Schema schema) {
		JsonGenericRecordReader recordReader = new JsonGenericRecordReader();
		return recordReader.read(data, schema);
	}

	public static Record readRecord(Schema oldSchema, String json) throws IOException {
		if (json == null) {
			return null;
		}
		GenericDatumReader<Record> reader = new GenericDatumReader<>();
		reader.setSchema(oldSchema);

		Decoder decoder = DecoderFactory.get().jsonDecoder(oldSchema, json);

		try {
			return reader.read(null, decoder);
		} catch (AvroTypeException e) {
			throw e;
		}
	}
	
	public static Record readRecord(Schema newSchema, Schema oldSchema, byte[] json) throws IOException {
		if (json == null) {
			return null;
		}
		GenericDatumReader<Record> reader = new GenericDatumReader<>();
		reader.setExpected(newSchema);
		reader.setSchema(oldSchema);

		Decoder decoder = DecoderFactory.get().binaryDecoder(json, null);

		try {
			return reader.read(null, decoder);
		} catch (AvroTypeException e) {
			throw e;
		}
	}

	public static Record readRecord(Schema schema, byte[] json) throws IOException {
		if (json == null) {
			return null;
		}
		GenericDatumReader<Record> reader = new GenericDatumReader<>();
		reader.setSchema(schema);

		Decoder decoder = DecoderFactory.get().binaryDecoder(json, null);

		try {
			return reader.read(null, decoder);
		} catch (AvroTypeException e) {
			throw e;
		}
	}

	public static Record readUndetailedJson(Schema schema, String json) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
		GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);


		writer.write(convertToGenericDataRecord(json.getBytes(), schema), encoder);
		encoder.flush();
		return readRecord(schema,outputStream.toByteArray());
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
