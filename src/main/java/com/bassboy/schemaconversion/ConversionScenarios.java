package com.bassboy.schemaconversion;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// class intended to be called by SchemaConditioner in a static fashion
// used to handle the scenarios that are not covered by avro libraries
public class ConversionScenarios {

	public static void oldSchema_unwrapRecordFromArray(BfsConditioner sc, Entry<String, JsonNode> oldEntry, Entry<String, JsonNode> latestEntry) {
		// TODO
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!! SCENARIO: record no longer wrapped in an array !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

	public static void oldSchema_wrapRecordInArray(BfsConditioner sc, Entry<String, JsonNode> oldEntry, Entry<String, JsonNode> latestEntry, JsonNode oldParent, JsonNode latestParent) throws IOException, SchemaConverterException {
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!! SCENARIO: record is now wrapped in an array !!!!!!!!!!!!!!!!!!!!!!!!!!!!");

		//modify old schema
		ObjectMapper mapper = new ObjectMapper();
		oldEntry.setValue(mapper.readTree("{\"type\":\"array\",\"items\":"+oldEntry.getValue()+",\"default\":[]}"));
		System.out.println("   oldSchema node modified: " + oldEntry);

		//modify old json
		String parentName = oldParent.get("name").toString().replace("\"", "");
		oldJson_modifyJsonRecord(sc,parentName);

	}

	private static void oldJson_modifyJsonRecord(BfsConditioner sc, String segmentName) throws IOException, SchemaConverterException {
		JsonNode json = sc.getOldJson();

		Map<String,JsonNode> map = new TreeMap<String, JsonNode>();
		map.put("start", json);
		Iterator<Entry<String, JsonNode>> mapIterator = map.entrySet().iterator();

		Entry<String, JsonNode> recordEntry = oldJson_obtainJsonNodeOfSegmentInRecord(mapIterator.next(),segmentName);

		ObjectMapper mapper = new ObjectMapper();
		try {
			recordEntry.setValue(mapper.readTree("[" + recordEntry.getValue() + "]"));
		}catch (java.lang.NullPointerException e){
			e.printStackTrace();
			throw new SchemaConverterException("old JSON does not match OLD schema\nNode not found in old JSON\n");
		}

		System.out.println("   oldJson node modified: " + recordEntry);

		sc.setOldJson(json);

	}

	private static Entry<String, JsonNode> oldJson_obtainJsonNodeOfSegmentInRecord(Entry<String, JsonNode> json, String segmentName) throws IOException {

		Entry<String, JsonNode> recordEntry = null;
		Entry<String, JsonNode> resultEntry = null;

		for (Iterator<Entry<String, JsonNode>> fields = json.getValue().fields(); fields.hasNext();) {
			recordEntry = fields.next();
			if(recordEntry.getKey().equals(segmentName)) {
				return recordEntry;
			}
			resultEntry = oldJson_obtainJsonNodeOfSegmentInRecord(recordEntry,segmentName);
			if (resultEntry!=null) return resultEntry;
		}
		return resultEntry;
	}

	public static void latestSchema_addAliasToFieldInArray(BfsConditioner sc, Entry<String, JsonNode> latestEntry, JsonNode oldArrayEntry, String latestPath) throws IOException {
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!! SCENARIO: renamed without alias in an array !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		ObjectMapper mapper = new ObjectMapper();
		String latestEntryValue = "[";

		for (JsonNode latestArrayEntry : latestEntry.getValue()) {
			if(sc.areTheSameRenamedObject(oldArrayEntry,latestArrayEntry)) {
				latestArrayEntry = mapper.readTree("{\"aliases\":[" + oldArrayEntry.get("name") + "],"+latestArrayEntry.toString().substring(1));
			}
			latestEntryValue = latestEntryValue+latestArrayEntry+",";
		}
		latestEntryValue = latestEntryValue.substring(0,latestEntryValue.length()-1) + "]";
		latestEntry.setValue(mapper.readTree(latestEntryValue));
		latestPath = latestPath.substring(0,latestPath.length()-1);
		latestSchema_updateEntryInSchema(latestEntry, latestPath, sc.getLatestSchema(), "/");
		return;
	}

	public static void latestSchema_addAliasToFieldInSchemaArray(BfsConditioner sc, Entry<String, JsonNode> latestEntry, Entry<String, JsonNode> oldEntry, String latestPath) throws IOException {

		JsonNode oldNode = oldEntry.getValue();
		JsonNode latestNode = latestEntry.getValue();

		JsonNode oldItems = oldNode.get("items");
		JsonNode latestItems = latestNode.get("items");
		JsonNode oldName = oldItems.get("name");

		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!! SCENARIO: renamed without alias in an object !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		ObjectMapper mapper = new ObjectMapper();

		latestItems = mapper.readTree("{\"aliases\":[" + oldName + "],"+latestItems.toString().substring(1));
		latestNode = mapper.readTree("{\"type\":\"array\",\"items\":"+latestItems+"}");
		latestEntry.setValue(latestNode);
		latestSchema_updateEntryInSchema(latestEntry, latestPath, sc.getLatestSchema(), "/");
		return;
	}

	private static void latestSchema_updateEntryInSchema(Entry<String, JsonNode> resultEntry, String latestPath, JsonNode latestParent,String searchPath) throws IOException {

		Entry<String, JsonNode> searchEntry;
		for (Iterator<Entry<String, JsonNode>> searchFields = latestParent.fields(); searchFields.hasNext();) {
			searchEntry = searchFields.next();


			switch (searchEntry.getValue().getNodeType()) {
				case OBJECT:
					String processingPath = "";
					if(!searchPath.endsWith("/"))
						processingPath = searchPath + "/" + searchEntry.getKey();
					else
						processingPath = searchPath + searchEntry.getKey();

					if(latestPath.trim().equals(processingPath+"/")) {
						searchEntry.setValue(resultEntry.getValue());
						return;
					} else if(latestPath.startsWith(processingPath)) {
						latestSchema_updateEntryInSchema(resultEntry, latestPath, searchEntry.getValue(), processingPath);
						return;
					}

					break;
				case ARRAY:
					int i = 0;
					for (JsonNode latestArrayEntry : searchEntry.getValue()) {
						if(!searchPath.endsWith("/"))
							processingPath = searchPath + "/" + searchEntry.getKey()+"/"+i;
						else
							processingPath = searchPath + searchEntry.getKey()+"/"+i;

						if(latestPath.trim().equals(searchPath + "/" + searchEntry.getKey()+"/")) {
							searchEntry.setValue(resultEntry.getValue());
							return;
						}else if(latestPath.startsWith(processingPath)) {
							latestSchema_updateEntryInSchema(resultEntry, latestPath, latestArrayEntry, processingPath);
							return;
						}
						i++;
					}
					break;
				default:
					break;
			}
		}

	}

	public static void latestSchema_addType(BfsConditioner sc, Entry<String, JsonNode> oldEntry, Entry<String, JsonNode> latestEntry, JsonNode oldArrayEntry) {

		// TODO
		//		System.out.println(oldEntry);
		//		System.out.println(latestEntry);
		//		System.out.println(oldArrayEntry);
		//		System.out.println("asdf");

	}

	public static void latestSchema_missingSensitiveKey(BfsConditioner sc, Throwable e1) {

		// TODO
		// this method is incomplete. The only thing it does for now is notify of the following:
		// if json object in latest schema is missing "default" when it used to previously have default, do a System.out.println statement
		// note that default may not be the only sensitive key in the avro schema language
		for (String difference : e1.getMessage().replace("\n", "").replace("\r", "").replace(" ", "").split(";")) {
			if (difference.length() > 10) {
				if (difference.substring(0, 10).equals("Unexpected")) {
					difference = difference.substring(difference.indexOf(":") + 1);
					if (difference.equals("default")) {
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!! SCENARIO: latest schema object is missing sensitive keys !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						System.out.println("   " + difference);
					}
				}
			}
		}
	}

}



