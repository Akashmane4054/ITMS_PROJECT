package com.ehr.assessment.business.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import org.springframework.util.MultiValueMap;

import com.ehr.assessment.business.dto.AssessmentDto;
import com.ehr.assessment.business.dto.ResponsesDTO;
import com.ehr.assessment.integration.domain.Assessment;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;

public interface AssessmentResponseService {

	Map<String, Object> saveAssessmentResponse(ResponsesDTO responsesDTO, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException;

	Map<String, Object> getAssessment(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException;

	AssessmentDto buildAssessmentDto(Assessment assessment, String dateFormat, String zone,
			MultiValueMap<String, String> headers, Long userId, Long companyId)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException;

	Map<String, Object> getElementWiseRatingForAssessment(Long assessmentId, Long elementId,
			MultiValueMap<String, String> headers) throws ContractException, TechnicalException, BussinessException;
}
