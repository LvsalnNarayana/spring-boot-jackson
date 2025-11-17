package com.example.spring_boot_jackson.models.polymorphic;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeName("cat")
@JsonTypeInfo( use = JsonTypeInfo.Id.CLASS, property = "@class")
public class Cat extends Animal {

    private boolean likesMilk;
}
