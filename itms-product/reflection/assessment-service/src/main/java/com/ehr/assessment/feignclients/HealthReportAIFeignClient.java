package com.ehr.assessment.feignclients;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.ehr.assessment.business.dto.SuggestionFromAIRequestDTO;
import com.ehr.assessment.fallbackfactory.HealthReportAIFeignClientFallbackFactory;
import com.ehr.core.util.EndPointReference;

@FeignClient(name = "health-report-ai", url = "http://localhost:2000", configuration = com.ehr.assessment.config.CustomFeignConfig.class, fallbackFactory = HealthReportAIFeignClientFallbackFactory.class)
@Component
public interface HealthReportAIFeignClient {

	@PostMapping(EndPointReference.GENERATE_HEALTH_REPORT)
	public Map<String, Object> generateHealthReport(@RequestBody SuggestionFromAIRequestDTO suggestionFromAIRequestDTO,
			@RequestHeader Map<String, String> headers);

}
