package com.example.spring_boot_jackson.runners;

import com.example.spring_boot_jackson.runners.interfaces.TopicTask;
import com.example.spring_boot_jackson.tasks.TreeModelTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
public class TreeModelRunner implements TopicTask {

    private final TreeModelTask task = new TreeModelTask();

    @Override
    public String topicName() {
        return "treemodel";
    }

    @Override
    public void executeMethod(String methodName) throws Exception {

        Method method = TreeModelTask.class.getMethod(methodName);

        System.out.println("Executing method: " + methodName + " under topic: treemodel");

        log.info(method.getName());
        method.invoke(task);
    }
}
