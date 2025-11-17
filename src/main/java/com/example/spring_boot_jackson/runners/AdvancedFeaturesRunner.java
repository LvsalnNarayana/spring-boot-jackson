package com.example.spring_boot_jackson.runners;

import com.example.spring_boot_jackson.runners.interfaces.TopicTask;
import com.example.spring_boot_jackson.tasks.AdvancedFeaturesTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
public class AdvancedFeaturesRunner implements TopicTask {

    private final AdvancedFeaturesTask task = new AdvancedFeaturesTask();

    @Override
    public String topicName() {
        return "advanced";
    }

    @Override
    public void executeMethod(String methodName) throws Exception {
        Method m = AdvancedFeaturesTask.class.getMethod(methodName);
        System.out.println("Executing method under topic=advanced: " + methodName);
        log.info("Executing advanced method: {}", methodName);
        m.invoke(task);
    }
}
