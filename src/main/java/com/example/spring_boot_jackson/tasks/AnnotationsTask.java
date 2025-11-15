package com.example.spring_boot_jackson.tasks;

import com.example.spring_boot_jackson.models.User;
import com.example.spring_boot_jackson.models.User.Address;
import com.example.spring_boot_jackson.models.User.Views;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;

public class AnnotationsTask {

    private final ObjectMapper mapper;

    public AnnotationsTask() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private User sample() {
        User u = new User();
        u.setId(10L);
        u.setUsername("narayana");
        u.setPassword("secret123");
        u.setEmail("n@example.com");
        u.setActive(true);
        u.setCreatedAt(LocalDateTime.of(2025, 1, 1, 8, 30));
        u.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 8, 30));
        u.setAddress(new Address("Bangalore", "India"));
        return u;
    }

    /*
     * ==============================================
     *  METHOD: demonstratePropertyNaming()
     *  TYPE: Serialization
     *  FLOW: Java Object ➜ JSON String (Snake Case + Custom Property Names)
     *
     *  DESCRIPTION:
     *	 Demonstrates @JsonProperty, @JsonNaming, and @JsonPropertyOrder.
     *	 These annotations restructure the JSON output:
     *
     *     - camelCase → snake_case (via @JsonNaming)
     *     - id → user_id (via @JsonProperty)
     *     - Keys emitted in deterministic order (via @JsonPropertyOrder)
     *
     *  BEFORE (Java Object):
     *	 User(id=10, username="narayana", ...)
     *
     *  AFTER (JSON Output):
     *	 {
     *	   "user_id": 10,
     *	   "username": "narayana",
     *	   "email": "n@example.com",
     *	   ...
     *	 }
     *
     *  NOTES:
     *	 - Naming strategy applies to all properties unless overridden with @JsonProperty.
     * ==============================================
     */
    public void demonstratePropertyNaming() throws JsonProcessingException {
        User u = sample();
        String json = mapper.writeValueAsString(u);
        System.out.println("Property Naming Demonstration:\n" + json);
    }

    /*
     * ==============================================
     *  METHOD: demonstrateAlias()
     *  TYPE: Deserialization
     *  FLOW: Alternate JSON Keys ➜ Java Field Mapping
     *
     *  DESCRIPTION:
     *	 Shows how @JsonAlias allows backward compatibility by accepting
     *	 multiple JSON field names for a single Java field.
     *
     *  INPUT (JSON):
     *	   {"login": "alpha"}
     *
     *  AFTER:
     *	   user.username = "alpha"
     *
     *  NOTES:
     *	 - Useful during API migrations.
     *	 - Does not affect serialization—only input names.
     * ==============================================
     */
    public void demonstrateAlias() throws JsonProcessingException {
        String json = "{\"login\":\"alpha\"}";
        User u = mapper.readValue(json, User.class);
        System.out.println("Alias resolved username = " + u.getUsername());
    }

    /*
     * ==============================================
     *  METHOD: demonstrateUnwrapped()
     *  TYPE: Serialization
     *  FLOW: Nested Object ➜ Flattened Parent JSON
     *
     *  DESCRIPTION:
     *	 @JsonUnwrapped(prefix = "addr_") flattens Address fields into
     *	 the parent structure.
     *
     *  BEFORE:
     *	   address.city = "Bangalore"
     *
     *  AFTER:
     *	   {
     *	     "addr_city": "Bangalore",
     *	     "addr_country": "India",
     *	     ...
     *	   }
     *
     *  NOTES:
     *	 - No nested "address" object exists in output.
     * ==============================================
     */
    public void demonstrateUnwrapped() throws JsonProcessingException {
        User u = sample();
        String json = mapper.writeValueAsString(u);
        System.out.println("Flattened Address with @JsonUnwrapped:\n" + json);
    }

    /*
     * ==============================================
     *  METHOD: demonstrateAnySetterGetter()
     *  TYPE: Deserialization + Serialization
     *  FLOW: Unknown JSON Fields ➜ Captured into Map ➜ Re-Serialized
     *
     *  DESCRIPTION:
     *	 @JsonAnySetter captures unrecognized JSON fields into a Map.
     *	 @JsonAnyGetter emits them back during serialization.
     *
     *  BEFORE (Input JSON):
     *	   { "user_id":5, "extraOne":"A", "extraTwo":123 }
     *
     *  AFTER:
     *	   user.additional() = {extraOne="A", extraTwo=123}
     *
     *  NOTES:
     *	 - Great for extensible or semi-structured JSON.
     * ==============================================
     */
    public void demonstrateAnySetterGetter() throws JsonProcessingException {
        String json =
                "{ \"user_id\":5, \"username\":\"x\", \"extraOne\":\"A\", \"extraTwo\":123 }";

        User u = mapper.readValue(json, User.class);
        System.out.println("Dynamic fields captured: " + u.additional());

        System.out.println("Re-output with dynamic fields:\n" +
                mapper.writeValueAsString(u));
    }

    /*
     * ==============================================
     *  METHOD: demonstrateIdentity()
     *  TYPE: Serialization
     *  FLOW: Circular Object Graph ➜ JSON with Object IDs
     *
     *  DESCRIPTION:
     *	 Demonstrates @JsonIdentityInfo which prevents infinite
     *	 recursion by replacing repeated references with an ID.
     *
     *  BEFORE:
     *	   a.friend = b
     *	   b.friend = a
     *
     *  AFTER (JSON):
     *	   {
     *	     "user_id":10,
     *	     "friend":{"user_id":999,"friend":10}
     *	   }
     *
     *  NOTES:
     *	 - Crucial for JPA bidirectional mappings.
     * ==============================================
     */
    public void demonstrateIdentity() throws JsonProcessingException {
        User a = sample();
        User b = sample();
        b.setId(999L);
        a.add("friend", b);
        b.add("friend", a);

        System.out.println("Identity-safe serialization:");
        System.out.println(mapper.writeValueAsString(a));
    }

    /*
     * ==============================================
     *  METHOD: demonstrateWriteOnly()
     *  TYPE: Serialization + Deserialization
     *  FLOW: password (input only) ➜ Hidden on output
     *
     *  DESCRIPTION:
     *	 Demonstrates @JsonProperty(access = WRITE_ONLY)
     *
     *  BEFORE:
     *	   user.password = "secret123"
     *
     *  AFTER (Serialization):
     *	   No password field in JSON.
     *
     *  AFTER (Deserialization):
     *	   password is accepted from JSON.
     *
     *  NOTES:
     *	 - Essential for safely handling sensitive info.
     * ==============================================
     */
    public void demonstrateWriteOnly() throws JsonProcessingException {
        User u = sample();
        String out = mapper.writeValueAsString(u);

        System.out.println("Password removed from JSON:\n" + out);

        String input = "{ \"password\":\"abc123\" }";
        User u2 = mapper.readValue(input, User.class);
        System.out.println("Password accepted on input: " + u2.getPassword());
    }

    /*
     * ==============================================
     *  METHOD: demonstrateJsonView()
     *  TYPE: Serialization (Filtered)
     *  FLOW: Java Object ➜ Role-Based JSON Views
     *
     *  DESCRIPTION:
     *	 Demonstrates @JsonView to control which properties appear
     *	 depending on the selected "view".
     *
     *  VIEWS:
     *	   Public: email only
     *	   Admin : email + active
     *
     *  BEFORE:
     *	   user.email = "n@example.com"
     *	   user.active = true
     *
     *  AFTER:
     *	   Public JSON  = {"email": "..."}
     *	   Admin JSON   = {"email": "...", "active": true}
     *
     *  NOTES:
     *	 - Used in REST APIs where different roles see different data.
     * ==============================================
     */
    public void demonstrateJsonView() throws JsonProcessingException {
        User u = sample();

        String publicJson =
                mapper.writerWithView(Views.Public.class).writeValueAsString(u);

        String adminJson =
                mapper.writerWithView(Views.Admin.class).writeValueAsString(u);

        System.out.println("Public View:\n" + publicJson);
        System.out.println("Admin View:\n" + adminJson);
    }
}
