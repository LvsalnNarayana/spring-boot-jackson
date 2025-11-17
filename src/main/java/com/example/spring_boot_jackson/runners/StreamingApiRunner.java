package com.example.spring_boot_jackson.runners;

import com.example.spring_boot_jackson.runners.interfaces.TopicTask;
import com.example.spring_boot_jackson.tasks.StreamingApiTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
public class StreamingApiRunner implements TopicTask {

    private final StreamingApiTask task = new StreamingApiTask();

    @Override
    public String topicName() {
        return "streaming";
    }

    @Override
    public void executeMethod(String methodName) throws Exception {

        Method method = StreamingApiTask.class.getMethod(methodName);

        System.out.println("Executing method: " + methodName + " under topic: streaming");

        log.info(method.getName());
        method.invoke(task);
    }
}
