package com.bassboy.debug;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonMain {

	public static void main(String[] args) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();

		ObjectNode childNode1 = mapper.createObjectNode();
		childNode1.put("name1", "val1");
		childNode1.put("name2", "val2");

		rootNode.set("obj1", childNode1);

		ObjectNode childNode2 = mapper.createObjectNode();
		childNode2.put("name3", "val3");
		childNode2.put("name4", "val4");

		rootNode.set("obj2", childNode2);

		ObjectNode childNode3 = mapper.createObjectNode();
		childNode3.put("name5", "val5");
		childNode3.put("name6", "val6");

		rootNode.set("obj3", childNode3);


		String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		System.out.println(jsonString);
	
	}
	
}

