package com.example.SpringMate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SpringMateApplication {

	public static void main(String[] args) {

		SpringApplication.run(SpringMateApplication.class, args);
	}

}
