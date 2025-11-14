package com.example.spring_boot_jackson.runners;

import com.example.spring_boot_jackson.runners.interfaces.TopicTask;
import com.example.spring_boot_jackson.tasks.ObjectMapperTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
public class ObjMapperRunner implements TopicTask {

	private final ObjectMapperTask task = new ObjectMapperTask();

	@Override
	public String topicName() {
		return "object_mapper";
	}

	@Override
	public void executeMethod(String methodName)
		  throws
		  Exception
	{

		Method method = ObjectMapperTask.class.getMethod(methodName);

		System.out.println("Executing method: " + methodName + " under topic: object_mapper");

		log.info(method.getName());
		method.invoke(task);
	}
}
