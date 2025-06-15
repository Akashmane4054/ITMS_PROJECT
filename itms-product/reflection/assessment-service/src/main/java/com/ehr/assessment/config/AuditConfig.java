package com.ehr.assessment.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
class AuditConfig {

	@Bean
	AuditorAware<Long> createAuditorProvider() {

		return new SecurityAuditor();
	}

	@Bean
	AuditingEntityListener createAuditingListener() {

		return new AuditingEntityListener();
	}

	public class SecurityAuditor implements AuditorAware<Long> {

		@Override
		public Optional<Long> getCurrentAuditor() {
			return Optional.of(1L);
		}
	}

}