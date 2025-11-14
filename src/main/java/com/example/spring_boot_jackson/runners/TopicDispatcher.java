package com.example.spring_boot_jackson.runners;

import com.example.spring_boot_jackson.runners.interfaces.TopicTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
public class TopicDispatcher implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(TopicDispatcher.class);

	private final ApplicationContext context;

	public TopicDispatcher(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public void run(String... args)
		  throws
		  Exception
	{

		log.info(
			  "CLI Arguments: {}",
			  Arrays.toString(args)
		);

		String topic = null;
		String method = null;

		for (String arg : args) {
			if (arg.startsWith("--topic=")) {
				topic = arg.substring("--topic=".length());
			}
			if (arg.startsWith("--method=")) {
				method = arg.substring("--method=".length());
			}
		}

		if (topic == null || method == null) {
			System.out.println("Usage: --topic=<topic_name> --method=<method_name>");
			return;
		}

		final String finalTopic = topic;
		final String finalMethod = method;

		logContextDiagnostics();

		// Resolve all TopicTask beans
		Map<String, TopicTask> topicBeans = context.getBeansOfType(TopicTask.class);
		log.info(
			  "TopicTask Beans Detected: {}",
			  topicBeans.keySet()
		);

		// Find matching topic
		TopicTask
			  task =
			  topicBeans.values()
			            .stream()
			            .filter(t -> t.topicName()
			                          .equalsIgnoreCase(finalTopic))
			            .findFirst()
			            .orElseThrow(() -> new RuntimeException("Unknown topic: " + finalTopic));

		log.info(
			  "Executing topic='{}' method='{}'",
			  finalTopic,
			  finalMethod
		);

		task.executeMethod(finalMethod);
	}

	private void logContextDiagnostics() {
		log.info("============== APPLICATION CONTEXT DIAGNOSTICS ==============");

		// Application name
		log.info(
			  "Application Name        : {}",
			  context.getApplicationName()
		);

		// Display name
		log.info(
			  "Display Name            : {}",
			  context.getDisplayName()
		);

		// Context ID
		log.info(
			  "Context ID              : {}",
			  context.getId()
		);

		// Parent context
		if (context.getParent() != null) {
			log.info(
				  "Parent Context          : {}",
				  context.getParent()
				         .getId()
			);
		}
		else {
			log.info("Parent Context          : null (root)");
		}

		// Example: getBean by name (if exists)
		try {
			Object envBean = context.getBean("environment");
			log.info(
				  "Bean Lookup 'environment' : {}",
				  envBean.getClass()
				         .getName()
			);
		}
		catch (Exception ignored) {
			log.info("Bean Lookup 'environment' : not found");
		}

		// List all TopicTask beans
		log.info(
			  "TopicTask Bean Names    : {}",
			  context.getBeansOfType(TopicTask.class)
			         .keySet()
		);

		// List all beans in container (optional heavy log)
		// log.info("All Bean Count: {}", context.getBeanDefinitionCount());

		// Example annotation discovery
		context.getBeansOfType(TopicTask.class)
		       .forEach((name, bean) -> {
			       log.info(
				         "Annotations on Bean '{}': {}",
				         name,
				         Arrays.toString(bean.getClass()
				                             .getAnnotations())
			       );
		       });

		log.info("============================================================");
	}
}
