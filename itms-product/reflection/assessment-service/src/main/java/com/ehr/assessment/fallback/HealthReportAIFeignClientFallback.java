package com.ehr.assessment.fallback;

import java.util.HashMap;
import java.util.Map;

import com.ehr.assessment.business.dto.SuggestionFromAIRequestDTO;
import com.ehr.assessment.feignclients.HealthReportAIFeignClient;
import com.ehr.core.util.ExceptionUtil;

import feign.FeignException;

public class HealthReportAIFeignClientFallback implements HealthReportAIFeignClient {

	private final Throwable cause;

	public HealthReportAIFeignClientFallback(Throwable cause) {
		this.cause = cause;
	}

	@Override
	public Map<String, Object> generateHealthReport(SuggestionFromAIRequestDTO suggestionFromAIRequestDTO,
			Map<String, String> headers) {
		Map<String, Object> map = new HashMap<>();
		if (cause instanceof FeignException) {
			map = ExceptionUtil.extractExceptionMessage(cause.getMessage());
		}
		return map;
	}

}
