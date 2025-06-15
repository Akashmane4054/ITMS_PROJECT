package com.ehr.report.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableJpaAuditing
class AuditConfig {

	@Bean
	public AuditorAware<Long> createAuditorProvider() {

		return new SecurityAuditor();
	}

	@Bean
	public AuditingEntityListener createAuditingListener() {

		return new AuditingEntityListener();
	}

	public class SecurityAuditor implements AuditorAware<Long> {

		@Override
		public Optional<Long> getCurrentAuditor() {
			return Optional.of(1L);
		}
	}

}