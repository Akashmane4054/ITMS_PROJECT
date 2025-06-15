package com.ehr.assessment.business.service;

import java.util.Map;

import org.springframework.util.MultiValueMap;

import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;

public interface AssessmentDashboardService {

	Map<String, Object> getParticipationByStatusAndAge(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> getSectionRecordsCalculation(Long assessmentId, Long sectionId,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException;

}
