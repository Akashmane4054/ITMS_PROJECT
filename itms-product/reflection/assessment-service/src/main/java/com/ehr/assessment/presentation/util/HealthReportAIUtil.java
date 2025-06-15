package com.ehr.assessment.presentation.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.ehr.assessment.business.dto.SuggestionFromAIRequestDTO;
import com.ehr.assessment.feignclients.HealthReportAIFeignClient;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.util.Constants;
import com.ehr.core.util.ExceptionUtil;
import com.ehr.core.util.LogUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class HealthReportAIUtil {

	private static final String CLASSNAME = HealthReportAIUtil.class.getSimpleName();

	private HealthReportAIFeignClient feignClient;

	public Map<String, Object> generateHealthReport(SuggestionFromAIRequestDTO suggestionFromAIRequestDTO)
			throws ContractException, BussinessException, TechnicalException {

		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> response = null;

		try {
			Map<String, String> headers = new HashMap<>();
			headers.put("Authorization", "Bearer HRA");
			headers.remove("content-length");

			response = ExceptionUtil
					.throwExceptionsIfPresent(feignClient.generateHealthReport(suggestionFromAIRequestDTO, headers));

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, Constants.SPACE);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

}
