package com.example.spring_boot_jackson.models.polymorphic;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * =======================================================
 * Demonstrates @JsonFilter
 *
 *  JSON Filters allow runtime selection of fields:
 *
 *      SimpleBeanPropertyFilter.filterOutAllExcept("name")
 *
 *  NOTES:
 *    - Often used with AOP, tenant-context, user-role context.
 *    - Does NOT enforce security; it controls representation only.
 * =======================================================
 */
@Getter
@Setter
@NoArgsConstructor
@JsonFilter("dynamicFilter")
public class PetOwner {

    private String ownerName;
    private int age;
    private String email;
    private Animal pet; // polymorphic field
}
