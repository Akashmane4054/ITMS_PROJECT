package com.ehr.companymanagement;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.ehr.companymanagement.integration.repository.**")
@EntityScan(basePackages = "com.ehr.companymanagement.integration.domain")
@EnableMongoRepositories(basePackages = "com.ehr.core.lifemart.repository.**")
@ComponentScan(basePackages = { "com.ehr.**", "com.ehr.core.**" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ehr.core.feignclients.**")
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}

	@PostConstruct
	public void init() {
		// setting spring boot default time zone
		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
		return applicationBuilder.sources(Application.class);

	}
}
