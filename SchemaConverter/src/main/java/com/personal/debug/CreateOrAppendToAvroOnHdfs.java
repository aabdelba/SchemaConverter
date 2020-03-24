package com.personal.debug;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;

//import com.personal.commonfunctions.FileUtilities;


public class CreateOrAppendToAvroOnHdfs {

	static String latestSchemaPath = System.getProperty("user.dir") + "/src/main/resources/schema/latest";
	static String oldSchemaPath = System.getProperty("user.dir") + "/src/main/resources/schema/old";
	static String jsonInput = System.getProperty("user.dir") + "/src/main/resources/LISAcceptableDiff.json";
	/*
	public static void main(String[] args) throws Exception {
			
		SchemaObject latestSchema = new SchemaObject();
		SchemaObject oldSchema = new SchemaObject();

		latestSchema.parseSchemaFromAvsc(latestSchemaPath + "/Full.avsc");
		oldSchema.parseSchemaFromAvsc(oldSchemaPath + "/Full.avsc");

		latestSchema.getSchema().toString(true);

		//String json = FileUtilities.convertFileContentToString(jsonInput);
		
		try {
			Schema avroSchemaVersion1 = oldSchema.getSchema();
			
			
			writeToSequenceFile(readFromSequenceFile(json, avroSchemaVersion1), avroSchemaVersion1);
			
			Schema avroSchemaVersion2 = latestSchema.getSchema();
			
			writeToSequenceFile(readFromSequenceFile(json, avroSchemaVersion2), avroSchemaVersion2);
			
			
		
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
		}
	}
	*/
	private static void writeToSequenceFile(GenericRecord json, Schema avroSchema ) throws IOException{
		
		DataFileWriter<GenericRecord> fileWriter = null;
		
		GenericDatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>();
		fileWriter = new DataFileWriter<GenericRecord>(datumWriter);

		fileWriter.close();
	}
	
	static GenericRecord readFromSequenceFile(String json, Schema schema) throws Exception {

		Decoder decoder = DecoderFactory.get().jsonDecoder(schema, json);

		// DatumReader<Object> reader = new GenericDatumReader<Object>(newSchema);
		DatumReader<Object> reader = new GenericDatumReader<Object>(schema);
		// // OR
		// reader.setSchema(newSchema);
		return (GenericRecord) reader.read(new Object(), decoder);

	}
}
