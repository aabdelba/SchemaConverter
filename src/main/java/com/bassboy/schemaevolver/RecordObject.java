package com.bassboy.schemaevolver;

import java.io.*;

import com.bassboy.common.ConfigProp;
import com.bassboy.common.RwUtils;
import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;

// objects of this type can read in schemas and store it in its own internal attribute
// the attribute in this class that the schema is stored in is of type Schema
public class RecordObject {

	// basic POJO starts here

	private ConfigProp prop;
	private Schema schema;
	private GenericData.Record record;

	public GenericData.Record getRecord() {
		return record;
	}

	public void setRecord(GenericData.Record record) {
		this.record = record;
	}

	public void setJson(String json) throws IOException {
		this.record = parseJson(schema,json);
	}

	public void setJson(Schema previousSchema, GenericData.Record json) throws IOException {
		this.record = parseJson(schema,previousSchema,json.toString());
	}

	public Schema getSchema() {
		return schema;
	}

	public JsonNode getSchemaAsJsonNode() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(schema.toString());
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	public RecordObject() throws IOException {
		init();
	}

	private void init() throws IOException {
		//InputStream inStream = new FileInputStream(System.getProperty("user.dir") + "/global-QA.properties");
		prop = ConfigProp.getInstance();
		schema = null;
		//prop.load(inStream);
	}

	public RecordObject(String fileStr) throws InvalidEntryException {
		this(new File(fileStr));
	}

	public RecordObject(File file) throws InvalidEntryException {
		try {
			init();
			parseSchemaFromAvsc(file);
		} catch(Exception e){
			throw new InvalidEntryException("Invalid schema: "+file.getName()+"\n"+e.getMessage());
		}
	}

	// basic POJO ends here



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
		}
		Parser parser = new Parser();
		setSchema(parser.parse(text));
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

	public GenericData.Record parseJson(Schema schema, String json) throws IOException {
		return parseJson(schema,schema,json);//schema and expectedSchema are the same
	}

	public GenericData.Record parseJson(Schema newSchema, Schema oldSchema, String json) throws IOException {
		if (json == null) {
			return null;
		}
		GenericDatumReader<GenericData.Record> reader = createReader(oldSchema);
		reader.setExpected(newSchema);
		return readRecord(oldSchema,reader,json);
	}

	private GenericData.Record readRecord(Schema schema, GenericDatumReader<GenericData.Record> reader, String json) throws IOException {
		Decoder decoder = DecoderFactory.get().jsonDecoder(schema, json);
		try {
			GenericData.Record record = reader.read(null, decoder);
			this.setRecord(record);
			return record;
		} catch (AvroTypeException e) {
			throw e;
		}
	}

	private GenericDatumReader<GenericData.Record> createReader(Schema schema) {
		GenericDatumReader<GenericData.Record> reader = new GenericDatumReader<>();
		reader.setSchema(schema);
		return reader;
	}

}