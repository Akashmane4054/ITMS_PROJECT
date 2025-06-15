package com.ehr.assessment.presentation.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ehr.assessment.business.dto.ResponsesDTO;
import com.ehr.assessment.business.service.AssessmentResponseService;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.util.EndPointReference;
import com.ehr.core.util.LogUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AssessmentResponseController {

	private final AssessmentResponseService assessmentResponseService;

	@PostMapping(value = { EndPointReference.SAVE_ASSESSMENT_RESPONSE })
	public Map<String, Object> saveAssessmentResponse(@RequestBody ResponsesDTO responsesDTO,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException {
		log.info(LogUtil
				.presentationLogger(EndPointReference.SAVE_ASSESSMENT_RESPONSE + ", responsesDTO " + responsesDTO));
		return assessmentResponseService.saveAssessmentResponse(responsesDTO, headers);
	}

	@PostMapping(value = { EndPointReference.GET_ASSESSMENT })
	public Map<String, Object> getassessment(@RequestParam("assessmentId") Long assessmentId,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.GET_ASSESSMENT + " assessmentId: " + assessmentId));
		return assessmentResponseService.getAssessment(assessmentId, headers);
	}

	@PostMapping(value = { EndPointReference.GET_ELEMENT_RATING_FOR_ASSESSMENT })
	public Map<String, Object> getElementWiseRatingForAssessment(@RequestParam("assessmentId") Long assessmentId,
			@RequestParam("elementId") Long elementId, @RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.GET_ELEMENT_RATING_FOR_ASSESSMENT + " assessmentId: "
				+ assessmentId + " elementId: " + elementId));
		return assessmentResponseService.getElementWiseRatingForAssessment(assessmentId, elementId, headers);
	}

}
