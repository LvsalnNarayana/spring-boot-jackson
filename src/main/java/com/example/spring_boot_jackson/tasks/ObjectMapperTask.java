package com.example.spring_boot_jackson.tasks;

import com.example.spring_boot_jackson.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;

public class ObjectMapperTask {

	private final ObjectMapper mapper;

	@Autowired
	public ObjectMapperTask() {
		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new JavaTimeModule());
		this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	private User sampleUser() {
		return new User(
			  1L,
			  "narayana",
			  "secret123",
			  "narayana@example.com",
			  true,
			  LocalDateTime.now(),
			  LocalDateTime.now()
		);
	}

	/*
	 * ==============================================
	 *  METHOD: serializeToString()
	 *  TYPE: Serialization
	 *  FLOW: Java Object  ➜  JSON Text String
	 *
	 *  DESCRIPTION:
	 *	 Converts a User object into a JSON-formatted string. This is the most common
	 *	 serialization pathway used in REST responses, logs, and debugging outputs.
	 *
	 *  BEFORE (Java Object):
	 *	 User(id=1, username="narayana", ...)
	 *
	 *  AFTER (JSON String):
	 *	 {"id":1,"username":"narayana","email":"narayana@example.com","active":true,...}
	 *
	 *  NOTES:
	 *	 - @JsonProperty(WRITE_ONLY) ensures password is excluded during serialization.
	 *	 - LocalDateTime formatting is controlled by JavaTimeModule + @JsonFormat.
	 * ==============================================
	 */
	public void serializeToString()
		  throws
		  JsonProcessingException
	{
		User user = sampleUser();
		String json = mapper.writeValueAsString(user);
		System.out.println("writeValueAsString:\n" + json);
	}

	/*
	 * ==============================================
	 *  METHOD: serializeToFile()
	 *  TYPE: Serialization
	 *  FLOW: Java Object  ➜  JSON File
	 *
	 *  DESCRIPTION:
	 *	 Writes a User object to a physical JSON file on disk. Useful in batch systems,
	 *	 backups, testing, or exporting domain objects to storage.
	 *
	 *  BEFORE (Java Object):
	 *	 User(id=1, username="narayana")
	 *
	 *  AFTER (File: user-output.json):
	 *	 {"id":1,"username":"narayana","email":"narayana@example.com",...}
	 * ==============================================
	 */
	public void serializeToFile()
		  throws
		  IOException
	{
		User user = sampleUser();
		File file = new File("user-output.json");
		mapper.writeValue(
			  file,
			  user
		);
		System.out.println("writeValue(File): user-output.json written");
	}

	/*
	 * ==============================================
	 *  METHOD: serializeToBytes()
	 *  TYPE: Serialization
	 *  FLOW: Java Object  ➜  byte[] (UTF-8 JSON)
	 *
	 *  DESCRIPTION:
	 *	 Converts a User object to a UTF-8 encoded byte array representing JSON data.
	 *	 Perfect for message queues, HTTP bodies, caching layers, and binary transports.
	 *
	 *  BEFORE:
	 *	 User object
	 *
	 *  AFTER (byte[]):
	 *	 [123, 34, 105, 100, 34, ...]   // Human-readable as JSON when printed
	 * ==============================================
	 */
	public void serializeToBytes()
		  throws
		  JsonProcessingException
	{
		User user = sampleUser();
		byte[] bytes = mapper.writeValueAsBytes(user);
		System.out.println("writeValueAsBytes:\n" + new String(bytes));
	}


	/*
	 * ==============================================
	 *  METHOD: deserializeFromString()
	 *  TYPE: Deserialization
	 *  FLOW: JSON Text String  ➜  Java Object
	 *
	 *  DESCRIPTION:
	 *	 Reads a JSON string and constructs a User object. Demonstrates the core
	 *	 deserialization pathway used when receiving JSON inputs.
	 *
	 *  BEFORE (JSON Input):
	 *	 {
	 *	   "id":2,
	 *	   "username":"test",
	 *	   "password":"abc",
	 *	   "email":"test@example.com",
	 *	   "active":true
	 *	 }
	 *
	 *  AFTER (Java Object):
	 *	 User(id=2, username="test", password="abc", ...)
	 *
	 *  NOTES:
	 *	 - Password field is WRITE_ONLY, so it is accepted during input.
	 *	 - Unknown fields cause errors unless FAIL_ON_UNKNOWN_PROPERTIES=false.
	 * ==============================================
	 */
	public void deserializeFromString()
		  throws
		  JsonProcessingException
	{
		String
			  json =
			  "{ \"id\":2, \"username\":\"test\", \"password\":\"abc\", " +
			  "\"email\":\"test@example.com\", \"active\":true, " +
			  "\"createdAt\":\"2023-05-10T10:20:30\", " +
			  "\"updatedAt\":\"2023-05-10T10:20:30\" }";

		User user = mapper.readValue(
			  json,
			  User.class
		);
		System.out.println("readValue(String): " + user.getUsername());
		System.out.println("Password should be WRITE_ONLY: " + user.getPassword());
	}

	/*
	 * ==============================================
	 *  METHOD: deserializeFromFile()
	 *  TYPE: Deserialization
	 *  FLOW: JSON File  ➜  Java Object
	 *
	 *  DESCRIPTION:
	 *	 Loads a JSON file from disk and maps it back to a User object.
	 *
	 *  BEFORE (File Contents):
	 *	 {"id":1, "username":"narayana", ...}
	 *
	 *  AFTER (Java Object):
	 *	 User(id=1, username="narayana")
	 * ==============================================
	 */
	public void deserializeFromFile()
		  throws
		  IOException
	{
		File file = new File("user-output.json");
		if (!file.exists()) {
			System.out.println("readValue(File): user-output.json not found");
			return;
		}

		User user = mapper.readValue(
			  file,
			  User.class
		);
		System.out.println("readValue(File): Loaded user = " + user.getUsername());
	}


	/*
	 * ==============================================
	 *  METHOD: deserializeList()
	 *  TYPE: Deserialization
	 *  FLOW: JSON Array String  ➜  List<User>
	 *
	 *  DESCRIPTION:
	 *	 Demonstrates reading JSON arrays into typed Java collections using TypeReference.
	 *
	 *  BEFORE (JSON):
	 *	 [
	 *	   {
	 *		 "id":5,
	 *		 "username":"alpha",
	 *		 "email":"alpha@example.com",
	 *		 "active":true
	 *	   }
	 *	 ]
	 *
	 *  AFTER (Java):
	 *	 List<User> containing 1 element.
	 * ==============================================
	 */
	public void deserializeList()
		  throws
		  JsonProcessingException
	{
		String
			  json =
			  "[{" +
			  "\"id\":5," +
			  "\"username\":\"alpha\"," +
			  "\"email\":\"alpha@example.com\"," +
			  "\"active\":true," +
			  "\"createdAt\":\"2023-01-01T09:00:00\"," +
			  "\"updatedAt\":\"2023-01-01T09:00:00\"" +
			  "}]";

		List<User> users = mapper.readValue(
			  json,
			  new TypeReference<List<User>>() {
			  }
		);
		System.out.println("readValue(List<User>): size=" + users.size());
	}

	/*
	 * ==============================================
	 *  METHOD: readTreeModel()
	 *  TYPE: Tree Model (JsonNode)
	 *  FLOW: JSON Text String  ➜  JsonNode  ➜  manual field extraction
	 *
	 *  DESCRIPTION:
	 *	 Parses JSON into a traversable tree structure. Useful when input shape is uncertain
	 *	 or you need dynamic field access without strict POJO binding.
	 *
	 *  BEFORE (JSON):
	 *	 {"id":10,"username":"treeUser","email":"tree@example.com"}
	 *
	 *  AFTER (JsonNode Operations):
	 *	 node.get("id")        → 10
	 *	 node.get("username") → "treeUser"
	 * ==============================================
	 */
	public void readTreeModel()
		  throws
		  JsonProcessingException
	{
		String json = "{\"id\":10,\"username\":\"treeUser\",\"email\":\"tree@example.com\"}";

		JsonNode node = mapper.readTree(json);

		System.out.println("readTree: id=" +
		                   node.get("id")
		                       .asLong());
		System.out.println("readTree: username=" +
		                   node.get("username")
		                       .asText());
	}

	/*
	 * ==============================================
	 *  METHOD: configureDeserialization()
	 *  TYPE: Mapper Configuration
	 *  EFFECT:
	 *	  Alters how the mapper behaves during future deserialization operations.
	 *
	 *  DESCRIPTION:
	 *	 Disables FAIL_ON_UNKNOWN_PROPERTIES so extra JSON fields do not cause exceptions.
	 *
	 *  BEFORE:
	 *	 JSON with unknown fields  → throws exception
	 *
	 *  AFTER:
	 *	 JSON with unknown fields  → ignored silently
	 * ==============================================
	 */
	public void configureDeserialization() {
		mapper.configure(
			  DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
			  false
		);
		System.out.println("FAIL_ON_UNKNOWN_PROPERTIES disabled");
	}

	/*
	 * ==============================================
	 *  METHOD: configureSerialization()
	 *  TYPE: Mapper Configuration
	 *  EFFECT:
	 *	 Enables pretty-printing and deterministic key ordering.
	 *
	 *  BEFORE:
	 *	 Compact JSON string with unpredictable key order.
	 *
	 *  AFTER:
	 *	 {
	 *	   "active" : true,
	 *	   "email" : "narayana@example.com",
	 *	   ...
	 *	 }
	 * ==============================================
	 */
	public void configureSerialization() {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
		System.out.println("Serialization configured: INDENT + ORDER_MAP_ENTRIES_BY_KEYS");
	}

	/*
	 * ==============================================
	 *  METHOD: setCustomDateFormat()
	 *  TYPE: Mapper Configuration
	 *  EFFECT:
	 *	 Applies SimpleDateFormat for legacy java.util.Date types.
	 *
	 *  DESCRIPTION:
	 *	 This does NOT override LocalDateTime — that is controlled by JavaTimeModule.
	 *
	 *  BEFORE:
	 *	 Default (timezone-dependent) formatting.
	 *
	 *  AFTER:
	 *	 yyyy-MM-dd
	 * ==============================================
	 */
	public void setCustomDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		mapper.setDateFormat(sdf);

		System.out.println("Custom date format applied: yyyy-MM-dd");
		System.out.println("Note: LocalDateTime uses JavaTimeModule by default unless overridden.");
	}


	/*
	 * ==============================================
	 *  METHOD: registerCustomModule()
	 *  TYPE: Mapper Extension
	 *  EFFECT:
	 *	 Installs a custom serializer for User objects.
	 *
	 *  BEFORE:
	 *	 Default Jackson-generated JSON.
	 *
	 *  AFTER:
	 *	 Custom output defined by UserSimpleSerializer.
	 * ==============================================
	 */
	public void registerCustomModule() {
		SimpleModule module = new SimpleModule();
		module.addSerializer(
			  User.class,
			  new UserSimpleSerializer()
		);
		mapper.registerModule(module);

		System.out.println("Custom User serializer module registered.");
	}
}
