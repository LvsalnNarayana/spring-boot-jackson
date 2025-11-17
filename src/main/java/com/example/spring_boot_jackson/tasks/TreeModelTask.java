package com.example.spring_boot_jackson.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

public class TreeModelTask {

    private final ObjectMapper mapper;

    public TreeModelTask() {
        this.mapper = new ObjectMapper();
    }

    private String sampleJson() {
        return """
        {
          "name": "Narayana",
          "age": 30,
          "address": {
            "city": "Bangalore",
            "country": "India"
          },
          "hobbies": ["chess", "coding", "travel"],
          "meta": {
            "deep": { "field": "value123" }
          }
        }
        """;
    }

    /*
     * ==============================================
     *  METHOD: demonstrateReadTree()
     *  TYPE: Tree Model Parsing
     *  FLOW: JSON String ➜ JsonNode Tree
     *
     *  DESCRIPTION:
     *      Parses raw JSON into an in-memory hierarchical tree.
     *      JsonNode acts like a DOM for JSON: flexible, dynamic,
     *      and ideal for schema-agnostic processing.
     *
     *  BEFORE (Input JSON):
     *      {"name": "Narayana", "age": 30}
     *
     *  AFTER:
     *      root.get("name") → "Narayana"
     *      root.get("age")  → 30
     *
     *  NOTES:
     *      - Throws on malformed JSON.
     *      - Good for config merging, dynamic API proxying.
     * ==============================================
     */
    public void demonstrateReadTree() throws JsonProcessingException {
        JsonNode root = mapper.readTree(sampleJson());
        System.out.println("Root Node:\n" + root);
    }

    /*
     * ==============================================
     *  METHOD: demonstratePath()
     *  TYPE: Safe Navigation
     *  FLOW: JsonNode ➜ MissingNode-safe Access
     *
     *  DESCRIPTION:
     *      path() never throws or returns null.
     *      If field is missing, it returns a MissingNode.
     *
     *  BEFORE:
     *      root.path("missing")
     *
     *  AFTER:
     *      MissingNode (asText("fallback") → "fallback")
     *
     *  NOTES:
     *      - Ideal when traversing deep paths.
     *      - Guarantees no NullPointerException.
     * ==============================================
     */
    public void demonstratePath() throws JsonProcessingException {
        JsonNode root = mapper.readTree(sampleJson());

        String city = root.path("address").path("city").asText();
        String missing = root.path("unknown").asText("fallback");

        System.out.println("City: " + city);
        System.out.println("Missing default: " + missing);
    }

    /*
     * ==============================================
     *  METHOD: demonstrateGet()
     *  TYPE: Direct Access
     *  FLOW: JsonNode ➜ Nullable Node
     *
     *  DESCRIPTION:
     *      get() returns null if a field is absent.
     *      Use it when you are certain the key exists.
     *
     *  BEFORE:
     *      root.get("name")
     *
     *  AFTER:
     *      JsonNode("Narayana")
     *
     *  NOTES:
     *      - Unlike path(), get() can be null.
     *      - Prefer path() for unknown JSON.
     * ==============================================
     */
    public void demonstrateGet() throws JsonProcessingException {
        JsonNode root = mapper.readTree(sampleJson());
        JsonNode nameNode = root.get("name");

        System.out.println("Name Node: " + nameNode);
    }

    /*
     * ==============================================
     *  METHOD: demonstrateAsPrimitives()
     *  TYPE: Conversion
     *  FLOW: JsonNode ➜ Primitive Types
     *
     *  DESCRIPTION:
     *      asText(), asInt(), asDouble() extract primitive
     *      representations with optional defaults.
     *
     *  BEFORE:
     *      root.path("age").asInt()
     *
     *  AFTER:
     *      30
     *
     *  NOTES:
     *      - Check type with isTextual(), isNumber().
     *      - Default fallback prevents crashes.
     * ==============================================
     */
    public void demonstrateAsPrimitives() throws JsonProcessingException {
        JsonNode root = mapper.readTree(sampleJson());

        String name = root.path("name").asText();
        int age = root.path("age").asInt(0);

        System.out.println("Name: " + name + " | Age: " + age);
    }

    /*
     * ==============================================
     *  METHOD: demonstrateFieldsIterator()
     *  TYPE: ObjectNode Iteration
     *  FLOW: ObjectNode ➜ Map-like Key–Value Iteration
     *
     *  DESCRIPTION:
     *      fields() provides iterable key-value pairs.
     *
     *  BEFORE:
     *      address = {city:"Bangalore", country:"India"}
     *
     *  AFTER (Iteration Output):
     *      city: Bangalore
     *      country: India
     *
     *  NOTES:
     *      - Requires ObjectNode (use isObject() to validate).
     * ==============================================
     */
    public void demonstrateFieldsIterator() throws JsonProcessingException {
        JsonNode root = mapper.readTree(sampleJson());
        ObjectNode address = (ObjectNode) root.path("address");

        System.out.println("Address fields:");
        for (Iterator<Map.Entry<String, JsonNode>> it = address.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> e = it.next();
            System.out.println(e.getKey() + ": " + e.getValue().asText());
        }
    }

    /*
     * ==============================================
     *  METHOD: demonstrateArrayIteration()
     *  TYPE: Array Navigation
     *  FLOW: ArrayNode ➜ Iterable Elements
     *
     *  DESCRIPTION:
     *      elements() exposes raw items in a JSON array.
     *
     *  BEFORE:
     *      ["chess", "coding", "travel"]
     *
     *  AFTER:
     *      chess
     *      coding
     *      travel
     *
     *  NOTES:
     *      - Add new items using add().
     *      - Check size() to validate array structure.
     * ==============================================
     */
    public void demonstrateArrayIteration() throws JsonProcessingException {
        JsonNode root = mapper.readTree(sampleJson());
        ArrayNode hobbies = (ArrayNode) root.path("hobbies");

        System.out.println("Hobbies:");
        for (JsonNode n : hobbies) {
            System.out.println("- " + n.asText());
        }
    }

    /*
     * ==============================================
     *  METHOD: demonstrateFindValue()
     *  TYPE: Recursive Search
     *  FLOW: JsonNode ➜ Deep Field Search
     *
     *  DESCRIPTION:
     *      findValue() scans all descendants for a key.
     *
     *  BEFORE:
     *      Search for "field"
     *
     *  AFTER:
     *      "value123"
     *
     *  NOTES:
     *      - Useful for complex config trees.
     *      - Avoid on very large structures (potentially expensive).
     * ==============================================
     */
    public void demonstrateFindValue() throws JsonProcessingException {
        JsonNode root = mapper.readTree(sampleJson());
        JsonNode deep = root.findValue("field");

        System.out.println("Deep Field Found: " + deep.asText());
    }

    /*
     * ==============================================
     *  METHOD: demonstrateAtPath()
     *  TYPE: JSON Pointer Navigation
     *  FLOW: JsonNode ➜ JSON Pointer ➜ Node Access
     *
     *  DESCRIPTION:
     *      JSON Pointer syntax allows absolute navigation.
     *      Example: /address/city
     *
     *  BEFORE:
     *      root.at("/address/city")
     *
     *  AFTER:
     *      "Bangalore"
     *
     *  NOTES:
     *      - Supports arrays: /hobbies/0
     *      - Extremely readable for deeply nested JSON.
     * ==============================================
     */
    public void demonstrateAtPath() throws JsonProcessingException {
        JsonNode root = mapper.readTree(sampleJson());
        JsonNode city = root.at("/address/city");

        System.out.println("City via JSON Pointer: " + city.asText());
    }

    /*
     * ==============================================
     *  METHOD: demonstratePutSetRemove()
     *  TYPE: Mutation
     *  FLOW: ObjectNode ➜ Add / Replace / Delete
     *
     *  DESCRIPTION:
     *      Demonstrates dynamic JSON mutation:
     *      - put() → primitives
     *      - set() → JsonNode
     *      - remove() → delete fields
     *
     *  BEFORE:
     *      {}
     *
     *  AFTER:
     *      {"name":"Narayana","address":{"city":"Bangalore","country":"India"}}
     *
     *  NOTES:
     *      - ObjectNode is mutable; JsonNode is not.
     * ==============================================
     */
    public void demonstratePutSetRemove() throws JsonProcessingException {

        ObjectNode obj = mapper.createObjectNode();
        obj.put("name", "Narayana");
        obj.put("age", 30);

        ObjectNode address = mapper.createObjectNode();
        address.put("city", "Bangalore");
        address.put("country", "India");

        obj.set("address", address);

        System.out.println("Before remove:\n" + obj);
        obj.remove("age");
        System.out.println("After remove:\n" + obj);
    }

    /*
     * ==============================================
     *  METHOD: demonstrateCreateNodes()
     *  TYPE: Node Construction
     *  FLOW: Empty Node ➜ Filled JSON Structure
     *
     *  DESCRIPTION:
     *      Shows how to manually build JSON using
     *      createObjectNode() and createArrayNode().
     *
     *  BEFORE:
     *      root = {}
     *
     *  AFTER:
     *      {"title":"Sample","list":["item1",42]}
     *
     *  NOTES:
     *      - Use for generating dynamic JSON responses.
     * ==============================================
     */
    public void demonstrateCreateNodes() {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode arr = mapper.createArrayNode();

        arr.add("item1");
        arr.add(42);

        root.put("title", "Sample");
        root.set("list", arr);

        System.out.println("Constructed Node:\n" + root);
    }
}
