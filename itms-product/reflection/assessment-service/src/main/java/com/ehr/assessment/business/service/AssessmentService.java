package com.ehr.assessment.business.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.ehr.assessment.business.dto.AssessmentDto;
import com.ehr.assessment.business.dto.PlayPauseAssessmentDTO;
import com.ehr.assessment.business.dto.RespondentDetailsDTO;
import com.ehr.assessment.business.dto.ScheduleAssessmentDTO;
import com.ehr.assessment.business.dto.TemplateMappingDTO;
import com.ehr.core.dto.ListingDto;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;

public interface AssessmentService {

	Map<String, Object> saveAssessment(AssessmentDto assessmentDto, MultiValueMap<String, String> headers)

			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> getAssessmentUsers(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	void sendReminders() throws BussinessException, ContractException, TechnicalException;

	Map<String, Object> deleteAssessment(Long assessmentId, Boolean status, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> findAllAssessmentPagination(ListingDto listingDto, Long assessmentStatus,
			MultiValueMap<String, String> headers) throws BussinessException, TechnicalException, ContractException;

	Map<String, Object> saveAssessmentTemplateMapping(TemplateMappingDTO mappingDTO,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> assessmentProgressMapping(TemplateMappingDTO mappingDTO, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> listAllRespondentUserAssessment(MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException;

	Map<String, Object> scheduleAssessment(ScheduleAssessmentDTO scheduleAssessmentDTO,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> playPauseAssessment(PlayPauseAssessmentDTO assessmentDto, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> bulkUploadExcelRespondentUser(Long assessmentId, MultipartFile file,
			MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException;

	Map<String, Object> getRespondentUserDetails(Long userId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	public Map<String, Object> updateRespondentProfile(RespondentDetailsDTO dto, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	public Map<String, Object> findPaginatedRespondentsByAssessment(ListingDto listingDto, Long assessmentId,
			MultiValueMap<String, String> headers) throws BussinessException, TechnicalException, ContractException;

	Map<String, Object> getAssessmentInsightsDashboard(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> getAssessmentInsightsDetails(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> getTemplateSectionsByAssessmentId(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

}
