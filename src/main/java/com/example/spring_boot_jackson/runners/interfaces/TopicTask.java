package com.example.spring_boot_jackson.runners.interfaces;

public interface TopicTask {

	void executeMethod(String methodName)
		  throws
		  Exception;

	String topicName();
}
