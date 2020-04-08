package com.bassboy.debug;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

public class CreateRecordFromSchemaMain {

	public static void main(String[] args) throws IOException {
		
		String resourcesDir = System.getProperty("user.dir") + "/src/main/resources";
		Schema schema = new Schema.Parser().parse(new File(resourcesDir+"/schema1.avsc"));
		
		GenericRecord user1 = new GenericData.Record(schema);
		user1.put("userid", "12345");

        Schema childSchema = user1.getSchema().getField("friends").schema().getElementType();
        List<GenericRecord> friendList = new ArrayList();

        GenericRecord friend1 = new GenericData.Record(childSchema);
        friend1.put("Name", "Flea");
        friend1.put("phoneNumber", "613-700-2089");
        friend1.put("email", "rhcp@gmail.com");
        friendList.add(friend1);
        user1.put("friends", friendList);
        
        GenericRecord friend2 = new GenericData.Record(childSchema);
        friend1.put("Name", "Anthony");
        friend1.put("phoneNumber", "613-291-2089");
        friend1.put("email", "bigbaws@gmail.com");
        friendList.add(friend1);
        user1.put("friends", friendList);
        
        System.out.println(user1);

	}
	
}
