package com.example.SpringMate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClient;

@EnableAsync
@EnableCaching
@SpringBootApplication
public class SpringMateApplication {

	public static void main(String[] args) {

		SpringApplication.run(SpringMateApplication.class, args);
	}

	@Bean
	public RestClient restClient() {
		return RestClient.builder().build();
	}

}
