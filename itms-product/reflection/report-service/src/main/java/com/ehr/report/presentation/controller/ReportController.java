package com.ehr.report.presentation.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.util.EndPointReference;
import com.ehr.core.util.LogUtil;
import com.ehr.report.business.service.ReportService;

import lombok.extern.slf4j.Slf4j;

/**
 * This Controller contain ReportService handlers
 *
 * @author SwapniL
 */
@RestController
@Slf4j
public class ReportController {

	private static final String ASSESSMENT_ID = "assessmentId";

	@Autowired
	private ReportService reportService;

	@PostMapping(EndPointReference.GET_ASSESSMENT_INDIVIDUAL_INSIGHT)
	public Map<String, Object> getAssessmentIndividualInsight(@RequestParam(ASSESSMENT_ID) Long assessmentId,
			@RequestHeader MultiValueMap<String, String> headers)
			throws ContractException, BussinessException, TechnicalException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.GET_ASSESSMENT_INDIVIDUAL_INSIGHT + " assessmentId " + assessmentId));
		return reportService.getAssessmentIndividualInsight(assessmentId, headers);
	}

	@PostMapping(EndPointReference.GENERATE_ASSESSMENT_CORPORATE_INSIGHT)
	public Map<String, Object> generateAssessmentCorporateInsight(@RequestParam("assessmentId") Long assessmentId,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.GENERATE_ASSESSMENT_CORPORATE_INSIGHT + " assessmentId " + assessmentId));
		return reportService.generateAssessmentCorporateInsight(assessmentId, headers);
	}

}
