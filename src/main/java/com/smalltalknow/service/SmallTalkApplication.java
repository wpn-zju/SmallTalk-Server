package com.smalltalknow.service;

import com.smalltalknow.service.storage.StorageProperties;
import com.smalltalknow.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@RestController
@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class SmallTalkApplication {
	private static final Logger logger = LoggerFactory.getLogger(SmallTalkApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SmallTalkApplication.class, args);
	}

	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		return new ServerEndpointExporter();
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return args -> storageService.init();
	}
}
