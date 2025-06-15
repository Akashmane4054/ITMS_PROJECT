package com.ehr.assessment.presentation.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ehr.assessment.business.dto.AssessmentDto;
import com.ehr.assessment.business.dto.PlayPauseAssessmentDTO;
import com.ehr.assessment.business.dto.RespondentDetailsDTO;
import com.ehr.assessment.business.dto.ScheduleAssessmentDTO;
import com.ehr.assessment.business.dto.TemplateMappingDTO;
import com.ehr.assessment.business.service.AssessmentService;
import com.ehr.core.dto.ListingDto;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.util.Constants;
import com.ehr.core.util.EndPointReference;
import com.ehr.core.util.LogUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AssessmentController {

	private static final String ASSESSMENT_ID = "assessmentId";

	private final AssessmentService assessmentService;

	@PostMapping(value = { EndPointReference.GET_USER_BY_ASSESSMENT })
	public Map<String, Object> getAssessmentUsers(@PathVariable(ASSESSMENT_ID) Long assessmentId,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.presentationLogger(EndPointReference.GET_USER_BY_ASSESSMENT));
		return assessmentService.getAssessmentUsers(assessmentId, headers);
	}

	@PostMapping(value = { EndPointReference.SAVE_ASSESSMENT })
	public Map<String, Object> saveAssessment(@RequestBody AssessmentDto assessmentDto,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.SAVE_ASSESSMENT));
		return assessmentService.saveAssessment(assessmentDto, headers);
	}

	@Scheduled(cron = "0 0 */6 * * *") // Runs every 6 hours
	@PostMapping("sendReminder")
	public String sendReminders() throws BussinessException, ContractException, TechnicalException {
		assessmentService.sendReminders();
		return "Reminders sent!";
	}

	@PostMapping({ EndPointReference.DELETE_ASSESSMENT })
	public Map<String, Object> deleteAssessment(@RequestParam(ASSESSMENT_ID) Long assessmentId,
			@RequestParam(defaultValue = "false", value = Constants.STATUS, required = false) Boolean status,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.presentationLogger(EndPointReference.DELETE_ASSESSMENT + ASSESSMENT_ID + assessmentId));
		return assessmentService.deleteAssessment(assessmentId, status, headers);
	}

	@PostMapping(EndPointReference.FIND_ALL_ASSESSMENT_PAGINATION)
	public Map<String, Object> findAllAssessmentPagination(@RequestBody ListingDto listingDto,
			@RequestParam(defaultValue = "0", value = "assessmentStatus", required = false) Long assessmentStatus,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.FIND_ALL_ASSESSMENT_PAGINATION + " listingDto=" + listingDto.toString()));
		return assessmentService.findAllAssessmentPagination(listingDto, assessmentStatus, headers);
	}

	@PostMapping(EndPointReference.ASSESSMENT_TEMPLATE_MAPPING)
	public Map<String, Object> saveAssessmentTemplateMapping(@RequestBody TemplateMappingDTO mappingDTO,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.ASSESSMENT_TEMPLATE_MAPPING + " mappingDTO { }" + mappingDTO.toString()));
		return assessmentService.saveAssessmentTemplateMapping(mappingDTO, headers);
	}

	@PostMapping(EndPointReference.ASSESSMENT_PROGRESS_MAPPING)
	public Map<String, Object> assessmentProgressMapping(@RequestBody TemplateMappingDTO mappingDTO,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.ASSESSMENT_PROGRESS_MAPPING + " mappingDTO { }" + mappingDTO.toString()));
		return assessmentService.assessmentProgressMapping(mappingDTO, headers);
	}

	@PostMapping(EndPointReference.LIST_ALL_RESPONDENT_USER_ASSESSMENT)
	public Map<String, Object> listAllRespondentUserAssessment(@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException {
		log.info(LogUtil.presentationLogger(EndPointReference.LIST_ALL_RESPONDENT_USER_ASSESSMENT));
		return assessmentService.listAllRespondentUserAssessment(headers);
	}

	@PostMapping(EndPointReference.SCHEDULE_ASSESSMENT)
	public Map<String, Object> scheduleAssessment(@RequestBody ScheduleAssessmentDTO dto,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException {
		log.info(LogUtil.presentationLogger(EndPointReference.SCHEDULE_ASSESSMENT + " dto { }" + dto.toString()));
		return assessmentService.scheduleAssessment(dto, headers);
	}

	@PostMapping(EndPointReference.PLAY_PAUSE_ASSESSMENT)
	public Map<String, Object> playPauseAssessment(@RequestBody PlayPauseAssessmentDTO assessmentDto,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException {
		log.info(LogUtil
				.presentationLogger(EndPointReference.PLAY_PAUSE_ASSESSMENT + " dto { }" + assessmentDto.toString()));
		return assessmentService.playPauseAssessment(assessmentDto, headers);
	}

	@PostMapping(value = { EndPointReference.BULK_UPLOAD_EXCEL_RESPONDENT_USER })
	public Map<String, Object> bulkUploadExcel(@RequestParam(ASSESSMENT_ID) Long assessmentId,
			@RequestParam("respondentUser") MultipartFile file, @RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, IOException, ContractException {
		log.info(LogUtil
				.presentationLogger(EndPointReference.BULK_UPLOAD_EXCEL_RESPONDENT_USER + ", respondentUser : {} "));
		return assessmentService.bulkUploadExcelRespondentUser(assessmentId, file, headers);
	}

	@PostMapping(value = { EndPointReference.GET_RESPONDENT_USER_DETAILS })
	public Map<String, Object> getRespondentUserDetails(@RequestParam("userId") Long userId,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.presentationLogger(EndPointReference.GET_RESPONDENT_USER_DETAILS));
		return assessmentService.getRespondentUserDetails(userId, headers);
	}

	@PostMapping(value = { "/updateRespondentProfile", EndPointReference.UPDATE_RESPONDENT_PROFILE })
	public Map<String, Object> updateRespondentProfile(@RequestBody RespondentDetailsDTO dto,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.UPDATE_RESPONDENT_PROFILE) + ", respondentDto {}", dto);
		return assessmentService.updateRespondentProfile(dto, headers);
	}

	@PostMapping(EndPointReference.FIND_PAGINATED_RESPONDENTS_BY_ASSESSMENT)
	public Map<String, Object> findPaginatedRespondentsByAssessment(@RequestBody ListingDto listingDto,
			@RequestParam(ASSESSMENT_ID) Long assessmentId, @RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.FIND_PAGINATED_RESPONDENTS_BY_ASSESSMENT + " listingDto=" + listingDto.toString()));
		return assessmentService.findPaginatedRespondentsByAssessment(listingDto, assessmentId, headers);
	}

	@PostMapping(value = { EndPointReference.GET_ASSESSMENT_INSIGHTS_DASHBOARD })
	public Map<String, Object> getAssessmentInsightsDashboard(@RequestParam(ASSESSMENT_ID) Long assessmentId,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.GET_ASSESSMENT_INSIGHTS_DASHBOARD + ", AssessmentId : {} " + assessmentId));
		return assessmentService.getAssessmentInsightsDashboard(assessmentId, headers);
	}

	@PostMapping(value = { EndPointReference.GET_ASSESSMENT_INSIGHTS_DETAILS })
	public Map<String, Object> getAssessmentInsightsDetails(@RequestParam(ASSESSMENT_ID) Long assessmentId,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.GET_ASSESSMENT_INSIGHTS_DETAILS + " assessmentId: " + assessmentId));
		return assessmentService.getAssessmentInsightsDetails(assessmentId, headers);
	}

	@PostMapping(value = { EndPointReference.GET_TEMPLATE_SECTIONS_BY_ASSESSMENT_ID })
	public Map<String, Object> getTemplateSectionsByAssessmentId(@RequestParam(ASSESSMENT_ID) Long assessmentId,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.GET_TEMPLATE_SECTIONS_BY_ASSESSMENT_ID + " assessmentId: " + assessmentId));
		return assessmentService.getTemplateSectionsByAssessmentId(assessmentId, headers);
	}

}
