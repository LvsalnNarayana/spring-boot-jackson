package com.example.spring_boot_jackson.models;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// ignored always
@JsonIgnoreProperties({"internalFlag"})
// skip nulls
@JsonInclude(JsonInclude.Include.NON_NULL)
// camel → snake_case
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
// ordered output
@JsonPropertyOrder({"id", "username", "email"})
// handle cycles
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "user_id"
)
public class User {

    @JsonProperty("user_id")
    // renaming
    private Long id;

    @JsonAlias({"login", "user"})
    // backwards-compat
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    // accepted on input only
    private String password;

    @JsonView(Views.Public.class)
    // visible for public
    private String email;

    @JsonView(Views.Admin.class)
    // visible only in Admin view
    private Boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    // formatted dates
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private transient boolean internalFlag = true;
    // ignored via @JsonIgnoreProperties

    // Dynamically captured fields
    @JsonIgnore
    private Map<String, Object> extra = new HashMap<>();
    // Flatten this object
    @JsonUnwrapped(prefix = "addr_")
    private Address address;

    @JsonAnySetter
    public void add(
            String key,
            Object value
    ) {
        extra.put(
                key,
                value
        );
    }

    @JsonAnyGetter
    public Map<String, Object> additional() {
        return extra;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {

        private String city;
        private String country;
    }

    public static class Views {

        public static class Public {

        }

        public static class Admin extends Public {

        }
    }
}
