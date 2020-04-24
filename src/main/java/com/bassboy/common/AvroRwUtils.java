package com.bassboy.common;

import com.bassboy.schemaevolver.InvalidEntryException;
import com.bassboy.schemaevolver.SchemaEvolverException;
import com.bassboy.schemaevolver.RecordObject;
import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;
import tech.allegro.schema.json2avro.converter.JsonGenericRecordReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class AvroRwUtils {


    public static void main(String[] args) throws InvalidEntryException, IOException, SchemaEvolverException {
        ConfigProp configProp = ConfigProp.getInstance();
        String inputPath=System.getProperty("user.dir")+configProp.getProperty("debug.dir");
        RecordObject oldSchema = new RecordObject(inputPath+"schema/schema1.avsc");

//		oldSchema.setJson(new File(inputPath+"record/record.avro"));
    }

    public static Object convertToGenericDataRecord(byte[] data, Schema schema) {
        JsonGenericRecordReader recordReader = new JsonGenericRecordReader();
        return recordReader.read(data, schema);
    }

    // static version of com.bassboy.schemaevolver.RecordObject.parseJson(newSchema,oldSchema,json)
    public static GenericData.Record readRecord(Schema newSchema, Schema oldSchema, byte[] json) throws IOException {
        if (json == null) {
            return null;
        }
        GenericDatumReader<GenericData.Record> reader = new GenericDatumReader<>();
        reader.setExpected(newSchema);
        reader.setSchema(oldSchema);

        Decoder decoder = DecoderFactory.get().binaryDecoder(json, null);

        try {
            return reader.read(null, decoder);
        } catch (AvroTypeException e) {
            throw e;
        }
    }

    // static version of com.bassboy.schemaevolver.RecordObject.parseJson(schema,json)
    public static GenericData.Record readRecord(Schema schema, byte[] json) throws IOException {
        if (json == null) {
            return null;
        }
        GenericDatumReader<GenericData.Record> reader = new GenericDatumReader<>();
        reader.setSchema(schema);

        Decoder decoder = DecoderFactory.get().binaryDecoder(json, null);

        try {
            return reader.read(null, decoder);
        } catch (AvroTypeException e) {
            throw e;
        }
    }

    public static GenericData.Record readJsonWithFieldTypeDefinition(Schema newSchema, Schema oldSchema, String json) throws IOException {
        ByteArrayOutputStream outputStream = readByteArrayOutputStreamFromJsonString(oldSchema,json);
        return readRecord(newSchema,oldSchema,outputStream.toByteArray());
    }

    public static GenericData.Record readJsonWithFieldTypeDefinition(Schema schema, String json) throws IOException {
        ByteArrayOutputStream outputStream = readByteArrayOutputStreamFromJsonString(schema,json);
        return readRecord(schema,outputStream.toByteArray());
    }

    public static ByteArrayOutputStream readByteArrayOutputStreamFromJsonString(Schema schema, String json) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
        GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);

        writer.write(convertToGenericDataRecord(json.getBytes(), schema), encoder);
        encoder.flush();
        return outputStream;
    }



}
