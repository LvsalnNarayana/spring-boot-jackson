package com.example.spring_boot_jackson.tasks;

import com.example.spring_boot_jackson.models.User;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class UserSimpleSerializer extends StdSerializer<User> {

	public UserSimpleSerializer() {
		super(User.class);
	}

	@Override
	public void serialize(User user, JsonGenerator gen, SerializerProvider provider) throws IOException {

		gen.writeStartObject();
		gen.writeNumberField("id", user.getId());
		gen.writeStringField("username", user.getUsername());
		gen.writeStringField("email", user.getEmail());
		gen.writeBooleanField("active", user.getActive());

		// Demonstrating custom formatting:
		if (user.getCreatedAt() != null) {
			gen.writeStringField("createdAt", user.getCreatedAt().toString());
		}

		gen.writeEndObject();
	}
}
