package com.subex.embeddedAI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.subex.embeddedAI.config", "com.subex.embeddedAI.service", "com.subex.embeddedAI.controller"})
public class EmbeddedAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmbeddedAiApplication.class, args);
	}

}
