package com.ehr.report.business.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;

@Service
public interface ReportService {

	Map<String, Object> getAssessmentIndividualInsight(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> generateAssessmentCorporateInsight(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

}
