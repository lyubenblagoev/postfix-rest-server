package com.lyubenblagoev.postfixrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.lyubenblagoev.postfixrest.configuration.MailServerConfiguration;

@SpringBootApplication
@EnableConfigurationProperties(MailServerConfiguration.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
