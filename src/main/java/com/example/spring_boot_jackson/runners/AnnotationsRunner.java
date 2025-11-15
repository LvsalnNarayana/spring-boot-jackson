package com.example.spring_boot_jackson.runners;

import com.example.spring_boot_jackson.runners.interfaces.TopicTask;
import com.example.spring_boot_jackson.tasks.AnnotationsTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
public class AnnotationsRunner implements TopicTask {

	private final AnnotationsTask task = new AnnotationsTask();

	@Override
	public String topicName() {
		return "annotations";
	}

	@Override
	public void executeMethod(String methodName) throws Exception {

		Method method = AnnotationsTask.class.getMethod(methodName);

		System.out.println("Executing method: " + methodName + " under topic: annotations");

		log.info(method.getName());
		method.invoke(task);
	}
}
