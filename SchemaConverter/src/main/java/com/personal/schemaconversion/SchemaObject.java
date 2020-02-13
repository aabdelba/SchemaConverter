
package com.personal.schemaconversion;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Parser;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.specific.SpecificDatumWriter;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.personal.utils.GeneralUtils;

// objects of this type can read in schemas and store it in its own internal attribute
// the attribute in this class that the schema is stored in is of type Schema
public class SchemaObject {

	// basic POJO starts here
	
	private Properties prop;
	private Schema schema;
	private String json;

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
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

	public Properties getProp() {
		return prop;
	}

	public void setProp(Properties prop) {
		this.prop = prop;
	}

	public SchemaObject() throws IOException {
		init();
	}

	private void init() throws IOException {
		//InputStream inStream = new FileInputStream(System.getProperty("user.dir") + "/global-QA.properties");
		prop = new Properties();
		schema = null;
		//prop.load(inStream);
	}

	public SchemaObject(String fileStr) throws IOException {
		init();
		parseSchemaFromAvsc(fileStr);
	}
	
	// basic POJO ends here
	
	

	public void parseSchemaFromAvsc(String fileStr) throws IOException {

		String text = "";
		Reader istream = new StringReader(GeneralUtils.convertFileContentToString(fileStr));

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

	public void matchToSchema(SchemaObject latestSchemaObject, Set<String> renamedFields) throws IOException, SchemaConverterException {
		
		SchemaConditioner schemaConditioner = SchemaConditioner.getInstance(this, latestSchemaObject, getJson(), renamedFields);//get singleton instance of SchemaConditioner
		
		schemaConditioner.startConversion();// this is the method to start the breadth-first process
		
		Parser parser = new Parser();//create new parser
		setSchema(parser.parse(schemaConditioner.getOldSchema().toString()));//set updated old schema
		setJson(schemaConditioner.getOldJson().toString());//set updated old json that has a modified structure if there were array wrappings/unwrappings in the schemas
		parser = new Parser();//reset the parser
		latestSchemaObject.setSchema(parser.parse(schemaConditioner.getLatestSchema().toString()));//set updated latest schema
		
	}

}

