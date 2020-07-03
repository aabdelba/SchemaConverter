
package com.bassboy.schemaevolver;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.avro.generic.GenericData;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

// objects of this type are used to iterate through the old and new schemas by treating each schema as a tree node and by using breadth-first
public class BfsConditioner {

	private static BfsConditioner bfsUtilsInstance = null;//singleton class
	private JsonNode oldSchema;
	private JsonNode latestSchema;

	private ArrayList<JsonNode> jsonRecords;
	private HashMap<String,String> renamedFields;

	private BfsConditioner(EvolvingRecords oldSchemaObject, EvolvingRecords latestSchemaObject, ArrayList<JsonNode> oldJsons, HashMap<String,String> renamedFields) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		setOldSchema(mapper.readTree(oldSchemaObject.getSchema().toString()));
		setLatestSchema(mapper.readTree(latestSchemaObject.getSchema().toString()));
		setJsonRecords(oldJsons);
		setRenamedFields(renamedFields);
	}

	public static BfsConditioner getInstance(EvolvingRecords oldSchemaObject, EvolvingRecords latestSchemaObject, HashMap<String,String> renamedFields) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		ArrayList<JsonNode> oldJsonNodes = new ArrayList<>();
		for (GenericData.Record record : oldSchemaObject.getRecords())
			oldJsonNodes.add(mapper.readTree(record.toString()));

		if(bfsUtilsInstance == null) {
			bfsUtilsInstance = new BfsConditioner(oldSchemaObject, latestSchemaObject, oldJsonNodes, renamedFields);
		} else {
			bfsUtilsInstance.setLatestSchema(mapper.readTree(latestSchemaObject.getSchema().toString()));
			bfsUtilsInstance.setOldSchema(mapper.readTree(oldSchemaObject.getSchema().toString()));
			bfsUtilsInstance.setJsonRecords(oldJsonNodes);
			bfsUtilsInstance.setRenamedFields(renamedFields);
		}
		return bfsUtilsInstance;
	}

	public JsonNode getOldSchema() {
		return oldSchema;
	}

	public void setOldSchema(JsonNode oldSchema) {
		this.oldSchema = oldSchema;
	}

	public JsonNode getLatestSchema() {
		return latestSchema;
	}

	public void setLatestSchema(JsonNode latestSchema) {
		this.latestSchema = latestSchema;
	}

	public ArrayList<JsonNode> getJsonRecords() {
		return jsonRecords;
	}

	public void setJsonRecords(ArrayList<JsonNode> jsonRecords) {
		this.jsonRecords = jsonRecords;
	}

	public void addJsonRecord(JsonNode oldJson) {
		jsonRecords.add(oldJson);
	}

	public void  addJsonRecord(String oldJson) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		jsonRecords.add(mapper.readTree(oldJson));
	}

	public HashMap<String, String> getRenamedFields() {
		return renamedFields;
	}

	public void setRenamedFields(HashMap<String, String> renamedFields) {
		this.renamedFields = renamedFields;
	}





	public void startConversion() throws IOException, SchemaEvolverException {

		for (String key:getRenamedFields().keySet()) {
			System.out.println(key);
			System.out.println(getRenamedFields().get(key));
		}
		for (JsonNode record : jsonRecords) {
			goInsideNode(oldSchema, latestSchema, "/");
		}
	}

	// recursive method to traverse through old and latest schemas using breadth-first
	private void goInsideNode(JsonNode oldParent, JsonNode latestParent, String latestPath) throws IOException, SchemaEvolverException {

		Entry<String, JsonNode> oldEntry = null;
		Entry<String, JsonNode> latestEntry = null;

		for (Iterator<Entry<String, JsonNode>> latestFields = latestParent.fields(); latestFields.hasNext();) {
			for (Iterator<Entry<String, JsonNode>> oldFields = oldParent.fields(); oldFields.hasNext();) {

				// try to increment the iterators
				try {
					oldEntry = oldFields.next();
					latestEntry = latestFields.next();
					oldEntry = skipIrrelevantAvroFields(oldFields, oldEntry);
					latestEntry = skipIrrelevantAvroFields(latestFields, latestEntry);
				} catch (NoSuchElementException e) {
					// one object has more elements than the other
					try {
						JSONAssert.assertEquals(latestParent.toString(), oldParent.toString(),JSONCompareMode.NON_EXTENSIBLE);
					} catch (AssertionError | Exception e1) {
						SchemaManipulator.latestSchema_missingSensitiveKey(this,e1);
					}
					break;
				} finally {
					if (oldEntry.getKey().equals(latestEntry.getKey()))
						compareNodes(oldEntry, latestEntry, oldParent, latestParent, latestPath);
				}
			}
		}
	}

	private void compareNodes(Entry<String, JsonNode> oldEntry, Entry<String, JsonNode> latestEntry, JsonNode oldParent, JsonNode latestParent, String latestPath)
			throws IOException, SchemaEvolverException {

		latestPath = latestPath+latestEntry.getKey()+"/";

		System.out.println("");
		System.out.println("old: " + oldEntry);
		System.out.println("new: " + latestEntry);
		System.out.println("path in new: " + latestPath);


		if(oldEntry.getValue().has("name") && latestEntry.getValue().has("name"))
			if(!(oldEntry.getValue().get("name").equals(latestEntry.getValue().get("name"))))
				searchForElement(oldParent, latestEntry.getValue(), latestParent, latestPath);


		if (!oldEntry.getValue().equals(latestEntry.getValue())) {

			checkType(oldEntry, latestEntry);
			JsonNodeType latestNodeType = latestEntry.getValue().getNodeType();
			switch (latestNodeType) {

				case OBJECT:
					JsonNode latestNode = latestEntry.getValue();
					JsonNode oldNode = oldEntry.getValue();
					JsonNode latestType = latestNode.get("type");
					JsonNode oldType = oldNode.get("type");

				if(latestType.toString().equals("\"record\"") && oldType.toString().equals("\"record\"")) {
					JsonNode latestName = latestNode.get("name");
					JsonNode oldName = oldNode.get("name");

					if (!oldName.equals(latestName) && !foundInAliases(oldName, latestEntry.getValue())) {
						if(areTheSameRenamedObject(oldEntry.getValue(),latestEntry.getValue())) {
							SchemaManipulator.latestSchema_addAliasToFieldInArray(this,latestEntry,oldEntry.getValue(),latestPath);
							startConversion();
							return;
						}
					}
//					ObjectMapper mapper = new ObjectMapper();
//					goInsideNode(mapper.readTree("{\"fields\":"+oldNode.get("fields")+"}"), mapper.readTree("{\"fields\":"+latestNode.get("fields")+"}"),latestPath);
					goInsideNode(oldNode,latestNode,latestPath+"type/");
				} else if(latestType.toString().equals("\"array\"") && oldType.toString().equals("\"array\"")) {

					JsonNode oldItems = oldNode.get("items");
					JsonNode latestItems = latestNode.get("items");
					JsonNode oldName = oldItems.get("name");
					JsonNode latestName = latestItems.get("name");

					if (!oldName.equals(latestName) && !foundInAliases(oldName, latestItems)) {

						if(areTheSameRenamedObject(oldItems,latestItems)) {
							SchemaManipulator.latestSchema_addAliasToFieldInSchemaArray(this,latestEntry,oldEntry,latestPath);
							startConversion();
							return;
						}
					}

					goInsideNode(oldItems, latestItems, latestPath);
				} else if (!latestType.equals(oldType)) {
					if (latestType.toString().equals("\"record\"") && oldType.toString().equals("\"array\"")) {
						SchemaManipulator.oldSchema_unwrapRecordFromArray(this, oldEntry, oldParent);
					} else if (latestType.toString().equals("\"array\"") && oldType.toString().equals("\"record\"")) {
						SchemaManipulator.oldSchema_wrapRecordInArray(this,oldEntry,oldParent);
					} else {
						throw (new SchemaEvolverException("Unhandled case\nold: " + oldNode + "\nnew: " + latestNode));
					}
					startConversion();
					return;
				} else {
					throw (new SchemaEvolverException("Unhandled case\nold: " + oldNode + "\nnew: " + latestNode));
				}

					break;// case

				case ARRAY:
					if (!nodesAreEqual(oldEntry,latestEntry)) {
						if(isArrayOfObjects(oldEntry,latestEntry))
							processArray(oldEntry,latestEntry,oldParent,latestParent,latestPath);
						else
							processArray(oldEntry,latestEntry);
					}
					break;

				default:
					if(!(oldEntry.getKey().equals("name") && latestEntry.getKey().equals("name")))
						throw (new SchemaEvolverException("Unhandled case\nold: " + oldEntry + "\nnew: " + latestEntry));
					break;
			}
		}
	}

	// process a JSON array that contains one or more JSON objects
	private void processArray(Entry<String, JsonNode> oldEntry, Entry<String, JsonNode> latestEntry, JsonNode oldParent, JsonNode latestParent, String latestPath) throws IOException, SchemaEvolverException {
		int i = 0;

		for (JsonNode latestArrayEntry : latestEntry.getValue()) {
			for (JsonNode oldArrayEntry : oldEntry.getValue()) {

				if(oldArrayEntry.getNodeType().equals(JsonNodeType.OBJECT) && latestArrayEntry.getNodeType().equals(JsonNodeType.OBJECT)) {

					if (oldArrayEntry.get("name").equals(latestArrayEntry.get("name")) || foundInAliases(oldArrayEntry.get("name"), latestArrayEntry)) {
						goInsideNode(oldArrayEntry, latestArrayEntry,latestPath+i+"/");
						break;
					} else {
						if(areTheSameRenamedObject(oldArrayEntry,latestArrayEntry)) {
							SchemaManipulator.latestSchema_addAliasToFieldInArray(this,latestEntry,oldArrayEntry, latestPath);
							startConversion();
							return;
						}
					}
				}
			}
			i++;
		}
	}

	// process a JSON array that does not contain any JSON objects
	private void processArray(Entry<String, JsonNode> oldEntry, Entry<String, JsonNode> latestEntry) {
		boolean arrayEntryWasFoundInLatest = false;

		for (JsonNode oldArrayEntry : oldEntry.getValue()) {
			for (JsonNode latestArrayEntry : latestEntry.getValue()) {
				if(oldArrayEntry.toString().equals(latestArrayEntry.toString()))
					arrayEntryWasFoundInLatest = true;
			}
			if (!arrayEntryWasFoundInLatest)
				SchemaManipulator.latestSchema_addType(this,oldEntry,latestEntry,oldArrayEntry);
			arrayEntryWasFoundInLatest = false;
		}
	}

	private Entry<String, JsonNode> skipIrrelevantAvroFields(Iterator<Entry<String, JsonNode>> fieldsIterator,
															 Entry<String, JsonNode> nodeEntry) {
		while (nodeEntry.getKey().equals("doc") || nodeEntry.getKey().equals("namespace")
				|| nodeEntry.getKey().equals("aliases"))
			nodeEntry = fieldsIterator.next();
		return nodeEntry;
	}

	///////////////////////////////////////////////////////////////////////////////////// BOOLEAN METHODS THAT HELP SchemaConverter /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	boolean areTheSameRenamedObject(JsonNode oldNode, JsonNode latestNode) {

		String oldName = oldNode.get("name").toString().replace("\"", "");
		String latestName = latestNode.get("name").toString().replace("\"", "");

		return checkRenamedList(oldName,latestName);
	}

	private boolean checkRenamedList(String oldName, String latestName) {
		HashMap<String, String> renamedFieldsMap = getRenamedFields();
		for (String key : renamedFieldsMap.keySet()) {
			if(latestName.equals(key) && oldName.equals(renamedFieldsMap.get(key)) )
				return true;
		}
		return false;
	}

	// object arrays may also contain null value, which is not an object. Must check all array elements for a json object
	private boolean isArrayOfObjects(Entry<String, JsonNode> oldEntry, Entry<String, JsonNode> latestEntry) {
		boolean isObjectArray = false;
		for (JsonNode latestArrayEntry : latestEntry.getValue()) {
			if(latestArrayEntry.getNodeType().equals(JsonNodeType.OBJECT))
				isObjectArray=true;
		}
		for (JsonNode oldArrayEntry : oldEntry.getValue()) {
			if(oldArrayEntry.getNodeType().equals(JsonNodeType.OBJECT))
				isObjectArray=true;
		}
		return isObjectArray;
	}

	private Boolean foundInAliases(JsonNode oldName, JsonNode latestNode) {
		Boolean aliasFound = false;

		if(latestNode.has("aliases")) {
			for(JsonNode alias : latestNode.get("aliases")) {
				if(alias.toString().replace("\"", "").equals(oldName.toString().replace("\"", "")))
					aliasFound = true;
			}
		}
		return aliasFound;
	}

	private Boolean nodesAreEqual(Entry<String, JsonNode> oldEntry, Entry<String, JsonNode> latestEntry) {
		if (latestEntry.getValue().equals(oldEntry.getValue()))
			return true;
		else
			return false;
	}

	private Boolean checkType(Entry<String, JsonNode> oldEntry, Entry<String, JsonNode> latestEntry)
			throws SchemaEvolverException {
		JsonNodeType latestNodeType = latestEntry.getValue().getNodeType();
		JsonNodeType oldNodeType = oldEntry.getValue().getNodeType();
		if (!(oldNodeType == latestNodeType))
			throw new SchemaEvolverException("type mismatch\noldNode: " + oldEntry + "\nlatestNode: " + latestEntry);
		else
			return true;
	}

	private void searchForElement(JsonNode oldParent, JsonNode latestNode, JsonNode latestParent, String latestPath) throws IOException, SchemaEvolverException {

		throw new SchemaEvolverException("Unhandled case: latest schema has extra record - do nothing since it is handled by avro\noldParent" + oldParent + "\nlatestParent: " + latestParent);
		// NOTE: this method has never been used during run-time
		// however, if it does get used, it may need debugging
		// For now, if this is ever encountered, throw exception. If this code is needed, uncomment the following lines


//		//search for oldEntry inside oldParent
//		//search result is true if oldEntry name is the same as latestEntry name
//		//uses aliases supplied from latestParent to search for oldEntry
//		Entry<String, JsonNode> searchEntry;
//		boolean nameWasFound = false;
//
//		for (Iterator<Entry<String, JsonNode>> oldFieldsSearch = oldParent.fields(); oldFieldsSearch.hasNext();) {
//			searchEntry = oldFieldsSearch.next();
//
//			if ( ((searchEntry.getKey().equals("name")) && searchEntry.getValue().equals(latestNode)) ||
//					foundInAliases(searchEntry.getValue(),latestParent) ||
//					areTheSameRenamedObject(searchEntry.getValue(), latestNode) ) {
//
//				nameWasFound = true;
//
//				ObjectMapper mapper = new ObjectMapper();
//				goInsideNode(searchEntry.getValue(), mapper.readTree("{\"name\":"+latestNode.get("fields")+"}"),latestPath);
//				break;
//			}
//		}
//
//		if (!nameWasFound) {
//			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!! latest schema has extra record - do nothing since it is handled by avro !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//			System.out.println("   " + latestNode);
//		}
	}
}
