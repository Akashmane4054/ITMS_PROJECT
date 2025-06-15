package com.ehr.assessment.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Request;

@Configuration
public class CustomFeignConfig {

	@Bean
	Request.Options feignRequestOptions() {
		return new Request.Options(5000, TimeUnit.SECONDS, 160000, TimeUnit.SECONDS, false);
	}
}
