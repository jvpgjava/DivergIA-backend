package com.divergia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DivergIAApplication {

	public static void main(String[] args) {
		SpringApplication.run(DivergIAApplication.class, args);
	}

}
