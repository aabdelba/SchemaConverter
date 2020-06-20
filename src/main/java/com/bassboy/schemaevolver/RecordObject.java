package com.bassboy.schemaevolver;

import java.io.*;

import com.bassboy.common.RwUtils;
import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;
import tech.allegro.schema.json2avro.converter.JsonGenericRecordReader;

// objects of this type can read in schemas and store it in its own internal attribute
// the attribute in this class that the schema is stored in is of type Schema
public class RecordObject {

	private Schema schema;
	private GenericData.Record record;

	public GenericData.Record getRecord() {
		return record;
	}

	public void setRecord(File recordFile) throws IOException {
		if(recordFile.getName().substring(recordFile.getName().indexOf(".")).equals(".avro"))
			setRecord(readAvroFile(recordFile));
		else
			setRecord(RwUtils.convertFileContentToString(recordFile));
	}

	public void setRecord(GenericData.Record record) {
		this.record = record;
	}

	public void setRecord(String json) throws IOException {
		this.record = parseJson(json);
	}

	public void setRecord(Schema expectedSchema, GenericData.Record json) throws IOException {
		this.record = parseJson(expectedSchema,json.toString());
	}
	
	public Schema getSchema() {
		return schema;
	}

	//access modifier is package-level because setSchema can introduce many defects if used incorrectly
	void setSchema(Schema schema) {
		this.schema = schema;
	}

	public RecordObject(String fileStr) throws InvalidSchemaEntryException {
		this(new File(fileStr));
	}

	public RecordObject(File file) throws InvalidSchemaEntryException {
		try {
			parseSchemaFromAvsc(file);
		} catch(Exception e){
			throw new InvalidSchemaEntryException("Schema parsing error: "+file.getName()+"\n"+e.getMessage());
		}
	}








	public void parseSchemaFromAvsc(File file) throws IOException {

		String text = "";
		Reader istream = new StringReader(RwUtils.convertFileContentToString(file));

		if (istream != null) {
			BufferedReader reader = new BufferedReader(istream);

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim().replace("\t", "");
				if (line.length() > 2) {
					if (!line.substring(0, 2).equals("//")) {
						text += line;
						text += System.getProperty("line.separator");
					}
				} else {
					text += line;
					text += System.getProperty("line.separator");
				}
			}
			reader.close();
			istream.close();
		}
		Parser parser = new Parser();
		setSchema(parser.parse(text));
	}

	public GenericData.Record readAvroFile(File file) throws IOException {
		GenericDatumReader datum = new GenericDatumReader();
		DataFileReader reader = new DataFileReader(file, datum);
		GenericData.Record record = new GenericData.Record(reader.getSchema());
		while (reader.hasNext()) {
			reader.next(record);
		}
		reader.close();
		return record;
	}

	public byte[] writeAvroToFile(String fullFilePath) throws IOException {

		GenericDatumWriter<Object> writer = new GenericDatumWriter<Object>(schema);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
		writer.write(record, encoder);
		encoder.flush();

		// write to a file
		DataFileWriter<Object> dataFileWriter = new DataFileWriter<>(writer);
		dataFileWriter.create(schema, new File(fullFilePath));
		dataFileWriter.append(record);
		System.out.println("Created " + fullFilePath);
		dataFileWriter.close();
		return outputStream.toByteArray();
//        return datum.toString();
	}

	public GenericData.Record parseJson(String json) throws IOException {
		return parseJson(schema,json);//schema and expectedSchema are the same
	}

	public GenericData.Record parseJson(Schema oldSchema, String json) throws IOException {
		if (json == null) {
			return null;
		}

		GenericDatumReader<GenericData.Record> reader = new GenericDatumReader<>();
		reader.setSchema(oldSchema);
		reader.setExpected(schema);//there is no need to set expected when it is the same as the setSchema

		Decoder decoder = DecoderFactory.get().jsonDecoder(oldSchema, json);
		try {
			GenericData.Record record = reader.read(null, decoder);
			this.setRecord(record);
			return record;
		} catch (AvroTypeException e) {
			throw e;
		}
	}


	@Deprecated
	public GenericData.Record readJsonWithFieldTypeDefinition(String json) throws IOException {
		ByteArrayOutputStream outputStream = readByteArrayOutputStreamFromJsonString(schema,json);
		return parseBinary(outputStream.toByteArray());
	}

	@Deprecated
	public GenericData.Record readJsonWithFieldTypeDefinition(Schema oldSchema, String json) throws IOException {
		ByteArrayOutputStream outputStream = readByteArrayOutputStreamFromJsonString(schema,json);
		return parseBinary(oldSchema,outputStream.toByteArray());
	}

	@Deprecated
	public ByteArrayOutputStream readByteArrayOutputStreamFromJsonString(Schema schema, String json) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
		GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);

		writer.write(convertJsonBytesToGenericDataRecord(json.getBytes(), schema), encoder);
		encoder.flush();
		return outputStream;
	}

	@Deprecated
	public Object convertJsonBytesToGenericDataRecord(byte[] data, Schema schema) {
		JsonGenericRecordReader recordReader = new JsonGenericRecordReader();
		return recordReader.read(data, schema);
	}

	@Deprecated
	public GenericData.Record parseBinary(byte[] json) throws IOException {
		return parseBinary(schema,json);//schema and expectedSchema are the same
	}

	@Deprecated
	public GenericData.Record parseBinary(Schema oldSchema, byte[] json) throws IOException {
		if (json == null) {
			return null;
		}
		GenericDatumReader<GenericData.Record> reader = new GenericDatumReader<>();
		reader.setSchema(oldSchema);
		reader.setExpected(schema);//there is no need to set expected when it is the same as the setSchema

		Decoder decoder = DecoderFactory.get().binaryDecoder(json, null);

		try {
			return reader.read(null, decoder);
		} catch (AvroTypeException e) {
			throw e;
		}
	}

}