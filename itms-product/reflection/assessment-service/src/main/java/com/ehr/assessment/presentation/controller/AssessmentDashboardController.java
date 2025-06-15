package com.ehr.assessment.presentation.controller;

import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ehr.assessment.business.service.AssessmentDashboardService;
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
public class AssessmentDashboardController {

	private static final String ASSESSMENT_ID = "assessmentId";
	private static final String SECTION_ID = "sectionId";

	private final AssessmentDashboardService dashboardService;

	@PostMapping(value = { EndPointReference.GET_PARTICIPATION_BY_STATUS_AND_AGE })
	public Map<String, Object> getParticipationByStatusAndAge(@RequestParam(ASSESSMENT_ID) Long assessmentId,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.GET_PARTICIPATION_BY_STATUS_AND_AGE + " assessmentId: " + assessmentId));
		return dashboardService.getParticipationByStatusAndAge(assessmentId, headers);
	}

	@PostMapping(value = { EndPointReference.GET_SECTION_RECORDS_CALCULATION })
	public Map<String, Object> getSectionRecordsCalculation(@RequestParam(ASSESSMENT_ID) Long assessmentId,
			@RequestParam(SECTION_ID) Long sectionId, @RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.presentationLogger(EndPointReference.GET_SECTION_RECORDS_CALCULATION + " assessmentId: "
				+ assessmentId + " & sectionId: " + sectionId));
		return dashboardService.getSectionRecordsCalculation(assessmentId, sectionId, headers);
	}

}
