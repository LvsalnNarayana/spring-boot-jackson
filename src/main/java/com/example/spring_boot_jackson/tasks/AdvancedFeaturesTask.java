package com.example.spring_boot_jackson.tasks;

import com.example.spring_boot_jackson.models.polymorphic.*;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import java.util.List;

/**
 * Demonstrates advanced Jackson features:
 * - Polymorphic serialization/deserialization
 * - Runtime filters via @JsonFilter
 * - Immutable builders via @JsonPOJOBuilder
 * - Type metadata strategies (Id.NAME vs Id.CLASS)
 *
 * All methods zero-arg for CLI invocation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdvancedFeaturesTask {

    private final ObjectMapper mapper;

    public AdvancedFeaturesTask() {
        this.mapper = new ObjectMapper();
        mapper.deactivateDefaultTyping(); // <<< CRITICAL FIX

        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /*
     * ==========================================================
     *  METHOD: demonstratePolymorphism()
     *  TYPE: Polymorphic Serialization/Deserialization
     *  FLOW: Java Object -> JSON (type annotated) -> Java Object
     *
     *  DESCRIPTION:
     *      Demonstrates Jackson polymorphic handling using:
     *      - @JsonTypeInfo(use = Id.NAME)
     *      - @JsonSubTypes
     *
     *  BEFORE (Java Objects):
     *      List<Animal> pets = [new Dog(), new Cat()]
     *
     *  AFTER (JSON):
     *      [
     *        {
     *          "@type": "dog",
     *          "name": "...",
     *          "bark_volume": 90
     *        },
     *        {
     *          "@type": "cat",
     *          "name": "...",
     *          "likesMilk": true
     *        }
     *      ]
     *
     *  NOTES:
     *      - Id.CLASS embeds fully-qualified class names (unsafe for APIs).
     *      - Id.NAME is recommended for REST APIs.
     * ==========================================================
     */
    public void demonstratePolymorphism() throws JsonProcessingException {
        System.out.println("Using mapper: " + mapper);
        System.out.println("USE_ANNOTATIONS enabled? " + mapper.isEnabled(MapperFeature.USE_ANNOTATIONS));
        System.out.println("Default Typing: " + mapper.getPolymorphicTypeValidator());
        Dog d = new Dog();
        d.setName("Bruno");
        d.setBarkVolume(85);

        Cat c = new Cat();
        c.setName("Kitty");
        c.setLikesMilk(true);

        List<Animal> animals = List.of(d, c);
        String json = mapper.writeValueAsString(animals);

        System.out.println("Polymorphic JSON:");
        System.out.println(json);

        List<Animal> back = mapper.readValue(json,
                mapper.getTypeFactory().constructCollectionType(List.class, Animal.class));

        System.out.println("Deserialized types:");
        back.forEach(a -> System.out.println(a.getClass().getSimpleName() + ": " + a.getName()));
    }

    /*
     * ==========================================================
     *  METHOD: demonstrateJsonFilter()
     *  TYPE: Runtime Filtering
     *  FLOW: Java Object -> FilterProvider -> JSON
     *
     *  DESCRIPTION:
     *      Demonstrates applying property filters at runtime
     *      using @JsonFilter on the target class.
     *
     *  BEFORE:
     *      owner = new PetOwner("Ravi", 30, "ravi@example.com", Dog)
     *
     *  AFTER (with filter):
     *      {"ownerName":"Ravi"}
     *
     *  NOTES:
     *      - Filters do NOT provide security; only view-level projection.
     *      - Common for audit logging, API shape control, masking sensitive fields.
     * ==========================================================
     */
    public void demonstrateJsonFilter() throws Exception {

        PetOwner owner = new PetOwner();
        owner.setOwnerName("Ravi");
        owner.setAge(30);
        owner.setEmail("ravi@example.com");

        Dog dog = new Dog();
        dog.setName("Tiger");
        dog.setBarkVolume(70);

        owner.setPet(dog);

        FilterProvider filters =
                new SimpleFilterProvider()
                        .addFilter("dynamicFilter",
                                SimpleBeanPropertyFilter.filterOutAllExcept("ownerName", "pet"));

        String json =
                mapper.writer(filters).writeValueAsString(owner);

        System.out.println("Filtered JSON:");
        System.out.println(json);
    }

    /*
     * ==========================================================
     *  METHOD: demonstratePojoBuilder()
     *  TYPE: Immutable Builder Deserialization
     *
     *  DESCRIPTION:
     *      Shows interpreting JSON into an immutable object
     *      using @JsonPOJOBuilder and builder pattern.
     *
     *  BEFORE (JSON):
     *      {
     *        "id": "R2D2",
     *        "model": "Astromech"
     *      }
     *
     *  AFTER (Java):
     *      Robot robot = new Robot(id=R2D2, model=Astromech)
     *
     *  NOTES:
     *      - @JsonPOJOBuilder(withPrefix = "") instructs Jackson
     *        to use builder methods without prefixes.
     * ==========================================================
     */
    public void demonstratePojoBuilder() throws Exception {

        String json = """
                {
                  "id": "R2D2",
                  "model": "Astromech"
                }
                """;

        Robot robot = mapper.readValue(json, Robot.class);

        System.out.println("Deserialized Robot:");
        System.out.println(robot.getId() + " - " + robot.getModel());
    }
}
