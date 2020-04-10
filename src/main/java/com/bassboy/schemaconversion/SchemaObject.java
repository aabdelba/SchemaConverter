package com.bassboy.schemaconversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import com.bassboy.utils.ConfigProp;
import com.bassboy.utils.RwUtils;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bassboy.utils.GeneralUtils;
import org.apache.avro.generic.GenericData;

// objects of this type can read in schemas and store it in its own internal attribute
// the attribute in this class that the schema is stored in is of type Schema
public class SchemaObject {

	// basic POJO starts here

	private ConfigProp prop;
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

	public SchemaObject() throws IOException {
		init();
	}

	private void init() throws IOException {
		//InputStream inStream = new FileInputStream(System.getProperty("user.dir") + "/global-QA.properties");
		prop = ConfigProp.getInstance();
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



}