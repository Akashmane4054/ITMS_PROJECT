package com.ehr.assessment.fallbackfactory;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.ehr.assessment.fallback.HealthReportAIFeignClientFallback;
import com.ehr.assessment.feignclients.HealthReportAIFeignClient;

@Component
public class HealthReportAIFeignClientFallbackFactory implements FallbackFactory<HealthReportAIFeignClient> {

	@Override
	public HealthReportAIFeignClient create(Throwable cause) {
		return new HealthReportAIFeignClientFallback(cause);
	}

}
