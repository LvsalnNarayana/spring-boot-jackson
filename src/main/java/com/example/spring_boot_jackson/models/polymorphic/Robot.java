package com.example.spring_boot_jackson.models.polymorphic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Getter;

/*
 * =======================================================
 *  Builder Pattern Support: @JsonPOJOBuilder
 *
 *  JSON:
 *      {
 *        "id": "R2D2",
 *        "model": "Astromech"
 *      }
 *
 *  BUILD:
 *      Robot r = new Robot.Builder()
 *                        .id("R2D2")
 *                        .model("Astromech")
 *                        .build();
 *
 *  NOTES:
 *    - Used for immutable or partially-immutable objects.
 *    - Fields must match builder method names unless overridden.
 * =======================================================
 */
@Getter
@JsonDeserialize(builder = Robot.Builder.class)
public class Robot {

    private final String id;
    private final String model;

    private Robot(Builder builder) {
        this.id = builder.id;
        this.model = builder.model;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private String id;
        private String model;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Robot build() {
            return new Robot(this);
        }
    }
}
