package com.bassboy.utils;

import java.io.*;

import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;

import tech.allegro.schema.json2avro.converter.JsonGenericRecordReader;

public final class RwUtils {

	public static byte[] writeAvro(String fullFilePath,Schema writerSchema, Object datum) throws IOException {

		GenericDatumWriter<Object> writer = new GenericDatumWriter<Object>(writerSchema);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Encoder e = EncoderFactory.get().binaryEncoder(outputStream, null);
		writer.write(datum, e);
		e.flush();

		// write to a file
		DataFileWriter<Object> dataFileWriter = new DataFileWriter<>(writer);
		dataFileWriter.create(writerSchema, new File(fullFilePath));
		dataFileWriter.append(datum);
		dataFileWriter.close();

		return outputStream.toByteArray();
	}

	public static Record readUndetailedJson(Schema newSchema, Schema oldSchema, String json) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
		GenericDatumWriter<Object> writer = new GenericDatumWriter<>(oldSchema);
		
		
		writer.write(convertToGenericDataRecord(json.getBytes(), oldSchema), encoder);
		encoder.flush();
		return read(newSchema,oldSchema,outputStream.toByteArray());
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

	public static Object convertToGenericDataRecord(byte[] data, Schema schema) {
	    JsonGenericRecordReader recordReader = new JsonGenericRecordReader();
		return recordReader.read(data, schema);
	}

	public static Record read(Schema oldSchema, String json) throws IOException {
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
	
	public static Record read(Schema newSchema, Schema oldSchema, byte[] json) throws IOException {
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

	public static Record readUndetailedJson(Schema schema, String json) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
		GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);
		
		
		writer.write(convertToGenericDataRecord(json.getBytes(), schema), encoder);
		encoder.flush();
		return read(schema,outputStream.toByteArray());
	}

	private static Record read(Schema schema, byte[] json) throws IOException {
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

	public static void writeJson(String fileStr, String jsonStr) throws IOException {
		Writer writer = new FileWriter(fileStr);
		writer.write(jsonStr);
		writer.close();
	}
}
