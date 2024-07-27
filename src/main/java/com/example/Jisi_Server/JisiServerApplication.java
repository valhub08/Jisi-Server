package com.example.Jisi_Server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.graphql.ConditionalOnGraphQlSchema;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class JisiServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JisiServerApplication.class, args);
	}

}
