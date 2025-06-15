package com.ehr.assessment.business.serviceimpl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.ehr.assessment.business.dto.AboutMeDTO;
import com.ehr.assessment.business.dto.AssessmentDto;
import com.ehr.assessment.business.dto.AssessmentElementResponseDto;
import com.ehr.assessment.business.dto.AssessmentWorkflowDto;
import com.ehr.assessment.business.dto.CategoryMasterDto;
import com.ehr.assessment.business.dto.ElementMasterValueDto;
import com.ehr.assessment.business.dto.ElementOptionsDto;
import com.ehr.assessment.business.dto.ElementsDto;
import com.ehr.assessment.business.dto.FamilyDTO;
import com.ehr.assessment.business.dto.PolicyDTO;
import com.ehr.assessment.business.dto.QuestionAndAnswersDTOForAISuggestion;
import com.ehr.assessment.business.dto.QuestionnaireDTOForAISuggestion;
import com.ehr.assessment.business.dto.RespondentDetailDTO;
import com.ehr.assessment.business.dto.RespondentDetailsDTO;
import com.ehr.assessment.business.dto.RespondentUserProfileDetailsDTOForAISuggestion;
import com.ehr.assessment.business.dto.ResponsesDTO;
import com.ehr.assessment.business.dto.SectionDto;
import com.ehr.assessment.business.dto.SuggestionFromAIRequestDTO;
import com.ehr.assessment.business.dto.TemplateRulesDto;
import com.ehr.assessment.business.enums.AssessmentResponseStatus;
import com.ehr.assessment.business.enums.AssessmentResponseType;
import com.ehr.assessment.business.service.AssessmentResponseService;
import com.ehr.assessment.business.service.AssessmentService;
import com.ehr.assessment.business.validation.AssessmentServiceValidator;
import com.ehr.assessment.integration.domain.Assessment;
import com.ehr.assessment.integration.domain.AssessmentElementResponse;
import com.ehr.assessment.integration.domain.AssessmentSubmittedResponseUser;
import com.ehr.assessment.integration.domain.AssessmentUserMapping;
import com.ehr.assessment.integration.domain.AssessmentWorkFlow;
import com.ehr.assessment.integration.domain.CategoryMaster;
import com.ehr.assessment.integration.domain.ElementMasterValue;
import com.ehr.assessment.integration.domain.ElementOptions;
import com.ehr.assessment.integration.domain.Elements;
import com.ehr.assessment.integration.domain.RespondentDetails;
import com.ehr.assessment.integration.domain.Section;
import com.ehr.assessment.integration.domain.SectionWiseRating;
import com.ehr.assessment.integration.domain.SectionWiseRiskMatrix;
import com.ehr.assessment.integration.domain.SectionWiseStatusMapping;
import com.ehr.assessment.integration.domain.TemplateCategoryMapping;
import com.ehr.assessment.integration.domain.TemplateMaster;
import com.ehr.assessment.integration.repository.AssessmentElementResponseRepository;
import com.ehr.assessment.integration.repository.AssessmentRepository;
import com.ehr.assessment.integration.repository.AssessmentSubmittedUserRepository;
import com.ehr.assessment.integration.repository.AssessmentUserRepository;
import com.ehr.assessment.integration.repository.AssessmentWorkflowRepository;
import com.ehr.assessment.integration.repository.CategoryRepository;
import com.ehr.assessment.integration.repository.ElementMasterValueRepository;
import com.ehr.assessment.integration.repository.ElementOptionsRepository;
import com.ehr.assessment.integration.repository.ElementsRepository;
import com.ehr.assessment.integration.repository.RespondentDetailsRepository;
import com.ehr.assessment.integration.repository.SectionRepository;
import com.ehr.assessment.integration.repository.SectionWiseRatingRepository;
import com.ehr.assessment.integration.repository.SectionWiseRiskMatrixRepository;
import com.ehr.assessment.integration.repository.SectionWiseStatusMappingRepository;
import com.ehr.assessment.integration.repository.TemplateCategoryRepository;
import com.ehr.assessment.integration.repository.TemplateRepository;
import com.ehr.assessment.presentation.util.HealthReportAIUtil;
import com.ehr.assessment.presentation.util.PropsValue;
import com.ehr.core.dto.DocumentDto;
import com.ehr.core.dto.DocumentGetDto;
import com.ehr.core.dto.RawTypeFieldDTO;
import com.ehr.core.dto.SuccessResponse;
import com.ehr.core.dto.UserMasterDTO;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.feignclients.HealthCompanyServiceFeignProxy;
import com.ehr.core.util.Constants;
import com.ehr.core.util.DateUtil;
import com.ehr.core.util.DocumentUtil;
import com.ehr.core.util.ExceptionUtil;
import com.ehr.core.util.LocationUtil;
import com.ehr.core.util.LogUtil;
import com.ehr.core.util.ObjectUtils;
import com.ehr.core.util.SvmUtil;
import com.ehr.core.util.UserUtil;
import com.google.gson.Gson;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AssessmentResponseServiceImpl implements AssessmentResponseService {

	private static final String CLASSNAME = AssessmentServiceImpl.class.getSimpleName();

	private static final String NO_SUCH_ASSESSMENT_EXIST = "no such assessment exist";
	private static final String ASSESSMENT_FOUND = "Assessment found";
	private static final String ASSESSMENT_DTO = "assessmentDto";
	private static final String TIMEZONE_IS = "Timezone is  ";
	public static final String DATE_FORMAT = "dd-MM-yyy";
	public static final String COMPLETED = "COMPLETED";
	public static final String PENDING = "PENDING";
	public static final String INPROGRESS = "INPROGRESS";
	private static final String ANALYZE_THIS_SECTION = "Analyze this section";
	private static String aiSuggestionOnAssessmentsResponseStructure = null;

	@PersistenceContext
	private EntityManager em;

	private final UserUtil userUtil;
	private final SvmUtil svmUtil;
	private final DocumentUtil documentUtil;
	private final TemplateServiceImpl templateServiceImpl;
	private final LocationUtil locationUtil;
	private final HealthCompanyServiceFeignProxy companyServiceFeignProxy;
	private final AssessmentUserRepository assessmentUserMappingRepository;
	private final AssessmentRepository assessmentRepository;
	private final AssessmentWorkflowRepository assessmentWorkflowRepository;
	private final AssessmentElementResponseRepository assessmentElementResponseRepository;
	private final SectionRepository sectionRepository;
	private final ElementsRepository elementRepository;
	private final ElementOptionsRepository elementOptionsRepository;
	private final ElementMasterValueRepository elementMasterValueRepository;
	private final RespondentDetailsRepository respondentDetailsRepository;
	private final SectionWiseRatingRepository sectionWiseRatingRepository;
	private final SectionWiseRiskMatrixRepository sectionWiseRiskMatrixRepository;
	private final TemplateRepository templateRepository;
	private final SectionWiseStatusMappingRepository sectionWiseStatusMappingRepository;
	private final TemplateCategoryRepository templateCategoryMappingRepository;
	private final CategoryRepository categoryRepository;
	private final AssessmentSubmittedUserRepository assessmentSubmittedUserRepository;
	private final HealthReportAIUtil healthReportAIUtil;
	private final PropsValue propsValue;

	@Lazy
	@Autowired
	private AssessmentService assessmentService;

	public AssessmentResponseServiceImpl(UserUtil userUtil, SvmUtil svmUtil, DocumentUtil documentUtil,
			TemplateServiceImpl templateServiceImpl, LocationUtil locationUtil,
			HealthCompanyServiceFeignProxy companyServiceFeignProxy,
			AssessmentUserRepository assessmentUserMappingRepository, AssessmentRepository assessmentRepository,
			AssessmentWorkflowRepository assessmentWorkflowRepository,
			AssessmentElementResponseRepository assessmentElementResponseRepository,
			SectionRepository sectionRepository, ElementsRepository elementRepository,
			ElementOptionsRepository elementOptionsRepository,
			ElementMasterValueRepository elementMasterValueRepository,
			RespondentDetailsRepository respondentDetailsRepository,
			SectionWiseRatingRepository sectionWiseRatingRepository,
			SectionWiseRiskMatrixRepository sectionWiseRiskMatrixRepository, TemplateRepository templateRepository,
			SectionWiseStatusMappingRepository sectionWiseStatusMappingRepository,
			TemplateCategoryRepository templateCategoryMappingRepository, CategoryRepository categoryRepository,
			AssessmentSubmittedUserRepository assessmentSubmittedUserRepository, HealthReportAIUtil healthReportAIUtil,
			PropsValue propsValue) {
		this.userUtil = userUtil;
		this.svmUtil = svmUtil;
		this.documentUtil = documentUtil;
		this.templateServiceImpl = templateServiceImpl;
		this.locationUtil = locationUtil;
		this.companyServiceFeignProxy = companyServiceFeignProxy;
		this.assessmentUserMappingRepository = assessmentUserMappingRepository;
		this.assessmentRepository = assessmentRepository;
		this.assessmentWorkflowRepository = assessmentWorkflowRepository;
		this.assessmentElementResponseRepository = assessmentElementResponseRepository;
		this.sectionRepository = sectionRepository;
		this.elementRepository = elementRepository;
		this.elementOptionsRepository = elementOptionsRepository;
		this.elementMasterValueRepository = elementMasterValueRepository;
		this.respondentDetailsRepository = respondentDetailsRepository;
		this.sectionWiseRatingRepository = sectionWiseRatingRepository;
		this.sectionWiseRiskMatrixRepository = sectionWiseRiskMatrixRepository;
		this.templateRepository = templateRepository;
		this.sectionWiseStatusMappingRepository = sectionWiseStatusMappingRepository;
		this.templateCategoryMappingRepository = templateCategoryMappingRepository;
		this.categoryRepository = categoryRepository;
		this.assessmentSubmittedUserRepository = assessmentSubmittedUserRepository;
		this.healthReportAIUtil = healthReportAIUtil;
		this.propsValue = propsValue;
	}

	static {
		try {
			InputStream inputStream = new ClassPathResource("json/AiSuggestionOnAssessmentsResponseStructure.json")
					.getInputStream();
			Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name());
			aiSuggestionOnAssessmentsResponseStructure = scanner.useDelimiter("\\A").next();
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Map<String, Object> verifyToken(MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		return ExceptionUtil.throwExceptionsIfPresent(userUtil.tokenVerification(headers));
	}

	@SuppressWarnings({})
	@Override
	public Map<String, Object> getAssessment(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException {

		log.info(LogUtil.startLog(CLASSNAME));

		Map<String, Object> response = new HashMap<>();

		Map<String, Object> userResponse = verifyToken(headers);

		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
		Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

		String timeZone = null;
		if (userResponse.get(Constants.USER_TIME_ZONE) != null) {
			timeZone = String.valueOf(userResponse.get(Constants.USER_TIME_ZONE));
		} else {
			timeZone = "(GMT-12:00) International Date Line West";
		}

		log.info(TIMEZONE_IS + timeZone);

		String zone = timeZone.substring(timeZone.indexOf("(") + 1, timeZone.indexOf(")"));

		log.info("Zone is  " + zone);

		Map<String, Object> resps = companyServiceFeignProxy.findCountryIdByCompanyId(companyId, headers);

		Long countryId = Long.parseLong(resps.get(Constants.COUNTRY_ID) + Constants.EMPTYSTRING);

		Map<String, Object> resps2 = ExceptionUtil
				.throwExceptionsIfPresent(locationUtil.findDateFormaterByCountryId(countryId));

		String dateFormat = String.valueOf(resps2.get(Constants.DATE_FORMAT));

		try {

			Assessment assessment = findAssessment(assessmentId, companyId);

			AssessmentDto assessmentDto = buildAssessmentDto(assessment, dateFormat, zone, headers, userId, companyId);
			response.put(ASSESSMENT_DTO, assessmentDto);
			response.put(Constants.SUCCESS, new SuccessResponse(ASSESSMENT_FOUND));
			response.put(Constants.ERROR, null);

		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	private Assessment findAssessment(Long assessmentId, Long companyId) throws BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		Assessment assessment = assessmentRepository.findByIdAndCompanyIdAndActive(assessmentId, companyId,
				Boolean.TRUE);
		if (assessment == null) {
			throw new BussinessException(HttpStatus.NOT_FOUND, NO_SUCH_ASSESSMENT_EXIST);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return assessment;
	}

	@Override
	public AssessmentDto buildAssessmentDto(Assessment assessment, String dateFormat, String zone,
			MultiValueMap<String, String> headers, Long userId, Long companyId)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException {
		log.info(LogUtil.startLog(CLASSNAME));
		AssessmentDto assessmentDto = new AssessmentDto();
		BeanUtils.copyProperties(assessment, assessmentDto);

		populateWorkflow(assessment.getId(), headers, zone, dateFormat, assessment, assessmentDto);

		List<SectionDto> sectionDto = getSectionDtoForGetAssessment(assessment, userId, assessmentDto, headers, zone);
		assessmentDto.setSections(sectionDto);

		updateAssessmentResponse(assessment.getId(), userId, sectionDto, assessmentDto);

		assessmentDto.setCategories(getCategoryMasterDtoList(assessment.getTemplateId()));

		List<TemplateRulesDto> templateRulesDto = templateServiceImpl.getTemplateRules(assessment.getTemplateId());
		assessmentDto.setRules(templateRulesDto);

		log.info(LogUtil.exitLog(CLASSNAME));
		return assessmentDto;
	}

	public List<CategoryMasterDto> getCategoryMasterDtoList(Long templateId) {
		List<TemplateCategoryMapping> templateCategoryMappings = templateCategoryMappingRepository
				.findByTemplateIdAndActive(templateId, Boolean.TRUE);

		List<CategoryMasterDto> categoryMasterListDto = new ArrayList<>();
		for (TemplateCategoryMapping categoryMapping : templateCategoryMappings) {
			CategoryMaster categoryMaster = categoryRepository.findByIdAndActive(categoryMapping.getCategoryId(),
					Boolean.TRUE);
			if (categoryMaster != null) {
				CategoryMasterDto categoryMasterDto = new CategoryMasterDto();
				BeanUtils.copyProperties(categoryMaster, categoryMasterDto);
				categoryMasterListDto.add(categoryMasterDto);
			}
		}
		return categoryMasterListDto;
	}

	@Override
	public Map<String, Object> getElementWiseRatingForAssessment(Long assessmentId, Long elementId,
			MultiValueMap<String, String> headers) throws ContractException, TechnicalException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> response = new HashMap<>();
		verifyToken(headers);
		try {
			List<AssessmentElementResponse> assessmentElementResponses = assessmentElementResponseRepository
					.findByAssessmentIdAndElementId(assessmentId, elementId);
			int elementResponseCount = assessmentElementResponses.size();
			response.put("elementResponseCount", elementResponseCount);

			List<ElementOptions> elementOptions = elementOptionsRepository.findByElementId(elementId);
			Map<String, Double> valueToNumericValueMap = elementOptions.stream().collect(
					Collectors.toMap(ElementOptions::getLabel, eo -> Double.parseDouble(eo.getOptionWeightage())));

			response.put("elementOptionWithWeightage", valueToNumericValueMap);

			double assessmentElementTotalWeightage = 0.0d;
			for (AssessmentElementResponse assessmentElementResponse : assessmentElementResponses) {
				assessmentElementTotalWeightage = assessmentElementTotalWeightage
						+ Double.parseDouble(assessmentElementResponse.getAssessmentWeightage());

			}
			response.put("assessmentElementTotalWeightage", assessmentElementTotalWeightage);

			BigDecimal assessmentElementAverage = (elementResponseCount != 0)
					? BigDecimal.valueOf(assessmentElementTotalWeightage).divide(
							BigDecimal.valueOf(elementResponseCount), 2, RoundingMode.HALF_UP)
					: BigDecimal.ZERO;

			response.put("assessmentElementAverage", assessmentElementAverage);
			response.put(Constants.SUCCESS, new SuccessResponse("Fetched Element Response Average!"));
			response.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private void populateWorkflow(Long assessmentId, MultiValueMap<String, String> headers, String zone,
			String dateFormat, Assessment assessment, AssessmentDto assessmentDto)
			throws BussinessException, ContractException, TechnicalException {

		log.info(LogUtil.startLog(CLASSNAME));

		List<AssessmentWorkFlow> mappingList = assessmentWorkflowRepository.findByAssessmentId(assessmentId);
		List<AssessmentWorkflowDto> mappingDtoList = new ArrayList<>();
		for (AssessmentWorkFlow work : mappingList) {
			AssessmentWorkflowDto mapDto = new AssessmentWorkflowDto();
			BeanUtils.copyProperties(work, mapDto);

			if (ObjectUtils.isPositiveNonZero(work.getCreatedBy())) {
				UserMasterDTO ownerUser = userUtil.getUserById(work.getCreatedBy(), headers);
				mapDto.setCreatedByName(ownerUser.getFullName());
				mapDto.setCreatedOn(DateUtil.convertToTimeZoneById(assessment.getCreatedOn(), dateFormat, zone));

			}

			if (ObjectUtils.isPositiveNonZero(work.getModifiedBy())) {
				UserMasterDTO ownerUser = userUtil.getUserById(work.getCreatedBy(), headers);
				mapDto.setModifiedByName(ownerUser.getFullName());
				mapDto.setModifiedOn(DateUtil.convertToTimeZoneById(assessment.getCreatedOn(), dateFormat, zone));

			}

			mappingDtoList.add(mapDto);
		}
		assessmentDto.setWorkflowDto(mappingDtoList);

		if (ObjectUtils.isPositiveNonZero(assessment.getReviewer())) {
			UserMasterDTO reviewerUser = userUtil.getUserById(assessment.getReviewer(), headers);
			assessmentDto.setReviewerName(reviewerUser.getFullName());
		}
		if (ObjectUtils.isPositiveNonZero(assessment.getScheduledBy())) {
			UserMasterDTO publisherUser = userUtil.getUserById(assessment.getScheduledBy(), headers);
			assessmentDto.setScheduledByName(publisherUser.getFullName());
			assessmentDto.setScheduledOn(DateUtil.convertToTimeZoneById(assessment.getScheduledOn(), dateFormat, zone));
		}
		if (ObjectUtils.isPositiveNonZero(assessment.getOwner())) {
			UserMasterDTO ownerUser = userUtil.getUserById(assessment.getOwner(), headers);
			assessmentDto.setOwnerName(ownerUser.getFullName());
		}

		if (ObjectUtils.isPositiveNonZero(assessment.getCreatedBy())) {
			UserMasterDTO ownerUser = userUtil.getUserById(assessment.getCreatedBy(), headers);
			assessmentDto.setCreatedBy(ownerUser.getFullName());
			assessmentDto.setCreatedOn(DateUtil.convertToTimeZoneById(assessment.getCreatedOn(), dateFormat, zone));

		}

		if (ObjectUtils.isPositiveNonZero(assessment.getModifiedBy())) {
			UserMasterDTO ownerUser = userUtil.getUserById(assessment.getModifiedBy(), headers);
			assessmentDto.setModifiedBy(ownerUser.getFullName());
			assessmentDto.setModifiedOn(DateUtil.convertToTimeZoneById(assessment.getModifiedOn(), dateFormat, zone));
		}

		assessmentDto.setExpiredOn(formatDate(assessment.getExpiredOn()));

		assessmentDto.setTemplateType(getTemplateType(assessment.getTemplateId()));

		assessmentDto.setUserIds(assessmentUserMappingRepository.findByAssessmentId(assessment.getId()));

		if (assessment.getLanguageId() != null) {

			String language = svmUtil.getValueBySerialId(assessment.getLanguageId());

			assessmentDto.setLanguage(language);

		}
		log.info(LogUtil.exitLog(CLASSNAME));
	}

	private Integer getTemplateType(Long templateId) {
		TemplateMaster templateMaster = templateRepository.findByIdAndActive(templateId, Boolean.TRUE);
		return templateMaster != null ? templateMaster.getTemplateType().getId() : null;
	}

	private List<SectionDto> getSectionDtoForGetAssessment(Assessment assessment, Long userId,
			AssessmentDto assessmentDto, MultiValueMap<String, String> headers, String zone)
			throws IOException, ContractException, TechnicalException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));

		List<Section> sections = sectionRepository.findByTemplateIdAndActive(assessment.getTemplateId(), Boolean.TRUE);
		List<SectionDto> sectionListDto = new ArrayList<>();
		int totalQuestionCount = 0;
		int totalResponseCountInOneAssessment = 0;
		double averageScore = 0.0;
		double responseWeightage = 0.0;
		double totalWeightageScore = 0.0;

		for (Section section : sections) {
			SectionDto sectionDto = new SectionDto();
			BeanUtils.copyProperties(section, sectionDto);

			if (ObjectUtils.isPositiveNonZero(section.getCreatedBy())) {
				UserMasterDTO ownerUser = userUtil.getUserById(section.getCreatedBy(), headers);
				sectionDto.setCreatedBy(ownerUser.getFullName());
				sectionDto.setCreatedOn(DateUtil.convertToTimeZoneById(assessment.getCreatedOn(), DATE_FORMAT, zone));

			}

			if (ObjectUtils.isPositiveNonZero(section.getModifiedBy())) {
				UserMasterDTO ownerUser = userUtil.getUserById(section.getModifiedBy(), headers);
				sectionDto.setModifiedBy(ownerUser.getFullName());
				sectionDto.setModifiedOn(DateUtil.convertToTimeZoneById(assessment.getModifiedOn(), DATE_FORMAT, zone));

			}

			List<Elements> elements = elementRepository.findBySectionIdAndActive(section.getId(), Boolean.TRUE);
			List<ElementsDto> elementsDto = new ArrayList<>();
			int responseCountInOneSection = 0;
			int questionCountInOneSection = 0;

			for (Elements element : elements) {

				ElementsDto elementDto = buildElementsDto(element, userId, assessment, headers, zone);

				AssessmentElementResponseDto dto = elementDto.getResponse();

				double weightage = 0.0;

				try {
					weightage = Double.parseDouble(Optional.ofNullable(element.getQuestionWeightage()).orElse("0.0"));
				} catch (NumberFormatException e) {
					weightage = 0.0;
				}

				if (dto != null && dto.getAssessmentWeightage() != null) {
					responseWeightage = dto.getAssessmentWeightage().stream().mapToDouble(Double::doubleValue).sum();
					totalWeightageScore += responseWeightage * weightage;
				}

				if (dto != null) {

					if ((Objects.equals(dto.getAssessmentResponseType(), AssessmentResponseType.SINGLE_CHOICE)
							|| Objects.equals(dto.getAssessmentResponseType(), AssessmentResponseType.MULTIPLE_CHOICE))
							&& (dto.getResponses() != null && !dto.getResponses().isEmpty())) {
						responseCountInOneSection++;
					}

					if (Objects.equals(dto.getAssessmentResponseType(), AssessmentResponseType.DOCUMENT_UPLOAD)
							&& dto.getDocumentDetailsDto() != null) {
						responseCountInOneSection++;
					}

				}
				elementsDto.add(elementDto);

			}
			sectionDto.setSectionElements(elementsDto);

			SectionWiseStatusMapping sectionWiseStatusMapping = sectionWiseStatusMappingRepository
					.findBysectionIdAndAssessmentIdAndUserId(section.getId(), assessment.getId(), userId);

			if (sectionWiseStatusMapping != null) {
				sectionDto.setSectionStatus(sectionWiseStatusMapping.getSectionResponseStatus());
				questionCountInOneSection = sectionWiseStatusMapping.getTotalQuestionInOneSection();

			} else {
				sectionDto.setSectionStatus(PENDING);
				questionCountInOneSection = elements.size();
			}

			sectionDto.setTotalQuestionInOneSection(questionCountInOneSection);
			sectionDto.setResponseCountInOneSection(responseCountInOneSection);

			totalQuestionCount += questionCountInOneSection;
			totalResponseCountInOneAssessment += responseCountInOneSection;

			sectionListDto.add(sectionDto);
		}

		averageScore = (totalQuestionCount > 0) ? (totalWeightageScore / totalQuestionCount) : 0.0;

		assessmentDto.setAverageScoreInOneAssessment(averageScore);
		assessmentDto.setTotalQuestionInOneAssessment(totalQuestionCount);
		assessmentDto.setTotalResponseCountInOneAssessment(totalResponseCountInOneAssessment);

		if (totalQuestionCount <= 0 && totalResponseCountInOneAssessment < 0
				|| totalResponseCountInOneAssessment <= totalQuestionCount) {

			double percentage = ((double) totalResponseCountInOneAssessment / totalQuestionCount) * 100;
			assessmentDto.setUserResponsePercent(percentage);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return sectionListDto;
	}

	private ElementsDto buildElementsDto(Elements element, Long userId, Assessment assessment,
			MultiValueMap<String, String> headers, String zone)
			throws TechnicalException, NumberFormatException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		ElementsDto elementDto = new ElementsDto();

		BeanUtils.copyProperties(element, elementDto);

		if (ObjectUtils.isPositiveNonZero(element.getCreatedBy())) {
			UserMasterDTO ownerUser = userUtil.getUserById(element.getCreatedBy(), headers);
			elementDto.setCreatedBy(ownerUser.getFullName());
			elementDto.setCreatedOn(DateUtil.convertToTimeZoneById(assessment.getCreatedOn(), DATE_FORMAT, zone));

		}

		if (ObjectUtils.isPositiveNonZero(element.getModifiedBy())) {
			UserMasterDTO ownerUser = userUtil.getUserById(element.getModifiedBy(), headers);
			elementDto.setModifiedBy(ownerUser.getFullName());
			elementDto.setModifiedOn(DateUtil.convertToTimeZoneById(assessment.getModifiedOn(), DATE_FORMAT, zone));

		}

		List<ElementOptionsDto> masters = fetchElementOptions(element);
		elementDto.setValues(masters);

		List<ElementMasterValueDto> masterValues = fetchElementMasterValues(element);
		elementDto.setMasterSetValues(masterValues);

		AssessmentElementResponseDto assessmentElementResponseDto = fetchElementResponse(element, assessment.getId(),
				userId);
		elementDto.setResponse(assessmentElementResponseDto);

		if (element.getFamilyResponseFlag() == Boolean.TRUE) {
			RespondentDetails respondentDetails = respondentDetailsRepository.findByUserId(userId);

			if (respondentDetails != null) {

				RespondentDetailsDTO respondentDetailsDTO = buildRespondentDetailsDTO(respondentDetails);

				elementDto.setRespondentDetailsDTO(respondentDetailsDTO);
			}
		}

		log.info(LogUtil.exitLog(CLASSNAME));
		return elementDto;
	}

	private List<ElementOptionsDto> fetchElementOptions(Elements element) {
		List<ElementOptions> masterList = elementOptionsRepository.findByElementId(element.getId());
		List<ElementOptionsDto> masters = new ArrayList<>();

		if (masterList != null && !masterList.isEmpty()) {
			for (ElementOptions master : masterList) {
				ElementOptionsDto elementOptionsDto = new ElementOptionsDto();
				BeanUtils.copyProperties(master, elementOptionsDto);
				masters.add(elementOptionsDto);
			}
		}

		return masters;
	}

	private List<ElementMasterValueDto> fetchElementMasterValues(Elements element) {
		List<ElementMasterValue> masterValueList = elementMasterValueRepository.findByElementId(element.getId());
		List<ElementMasterValueDto> masterValues = new ArrayList<>();

		if (masterValueList != null && !masterValueList.isEmpty()) {
			for (ElementMasterValue master : masterValueList) {
				ElementMasterValueDto elementMasterValueDto = new ElementMasterValueDto();
				BeanUtils.copyProperties(master, elementMasterValueDto);
				masterValues.add(elementMasterValueDto);
			}
		}

		return masterValues;
	}

	private AssessmentElementResponseDto fetchElementResponse(Elements element, Long assessmentId, Long userId)
			throws TechnicalException, NumberFormatException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));

		AssessmentElementResponseDto assessmentElementResponseDto = new AssessmentElementResponseDto();

		AssessmentElementResponse assessmentElementResponse = assessmentElementResponseRepository
				.findByCreatedByAndElementIdAndAssessmentId(userId, element.getId(), assessmentId);

		if (assessmentElementResponse == null) {
			log.info("No response found for user: " + userId + ", elementId: " + element.getId());
			return null;
		}

		BeanUtils.copyProperties(assessmentElementResponse, assessmentElementResponseDto);

		assessmentElementResponseDto.setSpouseResponse(formatResponse(assessmentElementResponse.getSpouseResponse()));

		assessmentElementResponseDto.setMotherResponse(formatResponse(assessmentElementResponse.getMotherResponse()));

		assessmentElementResponseDto.setFatherResponse(formatResponse(assessmentElementResponse.getFatherResponse()));

		assessmentElementResponseDto
				.setMotherinlawResponse(formatResponse(assessmentElementResponse.getMotherinlawResponse()));

		assessmentElementResponseDto
				.setFatherinlawResponse(formatResponse(assessmentElementResponse.getFatherinlawResponse()));

		assessmentElementResponseDto.setChild1Response(formatResponse(assessmentElementResponse.getChild1Response()));

		assessmentElementResponseDto.setChild2Response(formatResponse(assessmentElementResponse.getChild2Response()));

		AssessmentResponseType responseType = assessmentElementResponse.getAssessmentResponseType();
		String response = assessmentElementResponse.getResponse();

		if (Objects.equals(responseType, AssessmentResponseType.SINGLE_CHOICE)
				|| Objects.equals(responseType, AssessmentResponseType.MULTIPLE_CHOICE)) {

			if (StringUtils.isEmpty(response)) {
				assessmentElementResponseDto.setResponses(Collections.emptyList());
			} else {
				assessmentElementResponseDto
						.setResponses(Objects.equals(responseType, AssessmentResponseType.SINGLE_CHOICE)
								? Collections.singletonList(response)
								: Arrays.asList(response.split(",")));
			}
		}

		if (Objects.equals(assessmentElementResponse.getAssessmentResponseType(),
				AssessmentResponseType.DOCUMENT_UPLOAD)) {

			if (StringUtils.isEmpty(response)) {
				assessmentElementResponseDto.setResponses(Collections.emptyList());
			} else {
				if (assessmentElementResponse.getDocumentId() != null) {
					List<DocumentGetDto> docs = Collections.singletonList(documentUtil
							.getDocumentById(Long.parseLong(assessmentElementResponse.getDocumentId().trim())));

					assessmentElementResponseDto.setDocumentDetailsDto(docs);
				}
			}
		}

		if (assessmentElementResponse.getDocumentId() != null) {
			List<DocumentGetDto> docs = Collections.singletonList(
					documentUtil.getDocumentById(Long.parseLong(assessmentElementResponse.getDocumentId().trim())));

			assessmentElementResponseDto.setCommentsDocumentDto(docs);
		}

		if (StringUtils.isNotEmpty(assessmentElementResponse.getAssessmentWeightage())) {
			assessmentElementResponseDto.setAssessmentWeightage(
					Arrays.asList(Double.parseDouble(assessmentElementResponse.getAssessmentWeightage())));
		}

		log.info(LogUtil.exitLog(CLASSNAME));

		return assessmentElementResponseDto;
	}

	private RespondentDetailsDTO buildRespondentDetailsDTO(RespondentDetails details) {
		RespondentDetailsDTO dto = new RespondentDetailsDTO();

		AboutMeDTO aboutMeDTO = new AboutMeDTO();
		BeanUtils.copyProperties(details, aboutMeDTO);
		dto.setAboutMe(aboutMeDTO);

		FamilyDTO familyDTO = new FamilyDTO();
		BeanUtils.copyProperties(details, familyDTO);

		dto.setFamilyDTO(familyDTO);

		PolicyDTO policyDTO = new PolicyDTO();
		BeanUtils.copyProperties(details, policyDTO);
		dto.setPolicyDTO(policyDTO);

		dto.setUserId(details.getUserId());
		dto.setProfileImageId(details.getProfileImageId());

		return dto;
	}

	private List<String> formatResponse(String response) {
		if (response == null || response.trim().isEmpty()) {
			return Collections.emptyList();
		}
		return response.contains(",") ? Arrays.asList(response.split(",")) : Collections.singletonList(response);
	}

	private void updateAssessmentResponse(Long assessmentId, Long userId, List<SectionDto> sections,
			AssessmentDto assessmentDto) {

		AssessmentUserMapping assessmentUserMapping = assessmentUserMappingRepository
				.findByAssessmentIdAndUserId(assessmentId, userId);

		if (assessmentUserMapping != null) {
			int totalSections = sections.size();
			long pendingCount = sections.stream().filter(dto -> PENDING.equals(dto.getSectionStatus())).count();
			long inProgressCount = sections.stream().filter(dto -> INPROGRESS.equals(dto.getSectionStatus())).count();
			long completedCount = sections.stream().filter(dto -> COMPLETED.equals(dto.getSectionStatus())).count();

			if (inProgressCount > 0) {
				assessmentUserMapping.setAssessmentResponseStatus(AssessmentResponseStatus.INPROGRESS);
			} else if (pendingCount == totalSections) {
				assessmentUserMapping.setAssessmentResponseStatus(AssessmentResponseStatus.PENDING);
			} else if (completedCount == totalSections) {
				assessmentUserMapping.setAssessmentResponseStatus(AssessmentResponseStatus.COMPLETED);
			} else {
				assessmentUserMapping.setAssessmentResponseStatus(AssessmentResponseStatus.INPROGRESS);
			}

			assessmentUserMapping.setSubmittedOn(new Date());
			assessmentUserMapping = assessmentUserMappingRepository.save(assessmentUserMapping);
			assessmentDto.setAssessmentResponseStatus(assessmentUserMapping.getAssessmentResponseStatus().toString());
		}

	}

	@Override
	@Transactional
	public Map<String, Object> saveAssessmentResponse(ResponsesDTO responsesDTO, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException {

		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();

		double sectionTotal = 0.0d;
		String status = null;
		int totalQuestionInOneSection = 0;
		int responseCountInOneSection = 0;

		try {
			Map<String, Object> userResponse = verifyToken(headers);
			Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
			String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));

			boolean hasIncompleteMandatoryResponse = false;
			List<AssessmentElementResponse> responsesToSave = new ArrayList<>();

			List<AssessmentElementResponseDto> assessmentElementResponseList = responsesDTO.getElementResponseDtos();
			Long totalQuestionInOneAssessment = responsesDTO.getTotalQuestionInOneAssessment();
			Long totalResponseCountInOneAssessment = responsesDTO.getTotalResponseCountInOneAssessment();

			Long sectionId = assessmentElementResponseList.get(0).getSectionId();
			Long assessmentId = assessmentElementResponseList.get(0).getAssessmentId();

			assessmentElementResponseRepository.deleteAssessment(userId, sectionId, assessmentId);

			for (AssessmentElementResponseDto assessmentElementResponseDto : assessmentElementResponseList) {
				AssessmentServiceValidator.assessmentElementResponseValidation(assessmentElementResponseDto, language);
				AssessmentElementResponse assessmentElementResponse = mapDtoToEntity(assessmentElementResponseDto,
						userId);

				Double totalPoints = calculateTotalPoints(assessmentElementResponseDto.getAssessmentWeightage());
				sectionTotal = sectionTotal + totalPoints;
				assessmentElementResponse.setAssessmentWeightage(String.valueOf(totalPoints));

				Optional<Elements> elementOptional = elementRepository
						.findById(assessmentElementResponseDto.getElementId());
				Elements element = elementOptional.get();

				if (element != null && Boolean.TRUE.equals(element.getEnableCommentsFlag())) {
					assessmentElementResponse.setDocumentId(
							processDocumentDetails(assessmentElementResponseDto.getCommentsDocumentDto()));
				}

				if (Boolean.TRUE.equals(element.getMandatoryFlag())
						&& (assessmentElementResponseDto.getResponses() == null
								|| assessmentElementResponseDto.getResponses().isEmpty())) {
					hasIncompleteMandatoryResponse = true;
				}

				if (assessmentElementResponseDto.getResponses() != null
						&& !assessmentElementResponseDto.getResponses().isEmpty()) {
					responseCountInOneSection++;
				}

				responsesToSave.add(assessmentElementResponse);
			}

			totalQuestionInOneSection = assessmentElementResponseList.size();

			status = hasIncompleteMandatoryResponse ? INPROGRESS : COMPLETED;

			assessmentElementResponseRepository.saveAll(responsesToSave);

			updateTotalQuestionInOneAssessment(assessmentId, userId, totalQuestionInOneAssessment,
					totalResponseCountInOneAssessment);

			updateSectionWiseStatus(sectionId, assessmentId, userId, status, totalQuestionInOneSection,
					responseCountInOneSection);

			map.put(Constants.SUCCESS, new SuccessResponse("assessment response saved"));
			map.put(Constants.ERROR, null);

			Double finalSectionTotal = sectionTotal;
			log.info("SectionTotal: " + sectionTotal);

			CompletableFuture.runAsync(() -> {
				try {
					checkSectionComplete(assessmentElementResponseList.get(0), userId, finalSectionTotal);
					ifLastSectionSumbittedThenGenerateSuggestionFromAIAgainstAllTheSections(sectionId, userId, headers,
							assessmentId);
				} catch (TechnicalException | ContractException | BussinessException e) {
					throw new RuntimeException(e);
				}
			});

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	// As of now working in asynchronously will update when it's required.
	private void ifLastSectionSumbittedThenGenerateSuggestionFromAIAgainstAllTheSections(Long sectionId, Long userId,
			MultiValueMap<String, String> headers, Long assessmentId)
			throws TechnicalException, BussinessException, ContractException {

		Long templateId = sectionRepository.findTemplateIdBySectionIdAndActiveTrue(sectionId);

		Long lastSectionId = sectionRepository.findIdByTemplateIdAndActiveTrueOrderBySequenceDescLimit1(templateId);

		if (!sectionId.equals(lastSectionId)) {
			return;
		}

		List<Long> allSectionIdsAgainstTheTemplateId = sectionRepository.findIdsByTemplateId(templateId);

		List<RawTypeFieldDTO<Long, String, ?>> elementIdsAndResponse = assessmentElementResponseRepository
				.findElementIdAndResponseByCreatedByAndAssessmentId(userId, assessmentId);

		elementIdsAndResponse.removeIf(a -> StringUtils.isBlank(a.getField2()));

		Map<Long, String> responseByElementId = elementIdsAndResponse.stream().collect(Collectors
				.toMap(RawTypeFieldDTO<Long, String, ?>::getField1, RawTypeFieldDTO<Long, String, ?>::getField2));

		List<RawTypeFieldDTO<Long, Long, String>> sectionIdsAndElementIds = elementRepository
				.findActiveIdAndSectionIdAndLabelBySectionIdsIn(allSectionIdsAgainstTheTemplateId);

		Map<Long, List<Long>> elementIdsBySectionId = sectionIdsAndElementIds.stream()
				.collect(Collectors.groupingBy(RawTypeFieldDTO<Long, Long, String>::getField2, HashMap::new,
						Collectors.mapping(RawTypeFieldDTO<Long, Long, String>::getField1, Collectors.toList())));

		Map<Long, String> elementLabelsById = sectionIdsAndElementIds.stream().collect(Collectors
				.toMap(RawTypeFieldDTO<Long, Long, String>::getField1, RawTypeFieldDTO<Long, Long, String>::getField3));

		List<Map<String, QuestionnaireDTOForAISuggestion>> questionnaires = new ArrayList<>();

		for (Long sectionIdFromDb : allSectionIdsAgainstTheTemplateId) {
			QuestionnaireDTOForAISuggestion questionnaireDTOForAISuggestion = new QuestionnaireDTOForAISuggestion();
			questionnaireDTOForAISuggestion.setSuggestionRequired(ANALYZE_THIS_SECTION);
			questionnaireDTOForAISuggestion.setQuestionAndAnswers(setQuestionAndAnswers(elementLabelsById,
					responseByElementId, elementIdsBySectionId.get(sectionIdFromDb)));
			Map<String, QuestionnaireDTOForAISuggestion> questionnaire = new HashMap<>();
			questionnaire.put(String.valueOf(sectionIdFromDb), questionnaireDTOForAISuggestion);
			questionnaires.add(questionnaire);
		}

		SuggestionFromAIRequestDTO suggestionFromAIRequestDTO = new SuggestionFromAIRequestDTO();
		suggestionFromAIRequestDTO.setProfileDetails(setProfileDetails(userId, headers));
		suggestionFromAIRequestDTO.setQuestionnaire(questionnaires);
		suggestionFromAIRequestDTO.setExplanation(propsValue.explanationToAiForSuggestionOnAssessment);
		suggestionFromAIRequestDTO.setResponseStructure(aiSuggestionOnAssessmentsResponseStructure);

		Map<String, Object> healthReport = healthReportAIUtil.generateHealthReport(suggestionFromAIRequestDTO);

		log.info("Health report {}", new Gson().toJson(healthReport));
	}

	private List<QuestionAndAnswersDTOForAISuggestion> setQuestionAndAnswers(Map<Long, String> elementLabelById,
			Map<Long, String> responseByElementId, List<Long> elementsIds) {
		List<QuestionAndAnswersDTOForAISuggestion> questionAndAnswersDTOForAISuggestionList = new ArrayList<>();
		for (Long id : elementsIds) {
			QuestionAndAnswersDTOForAISuggestion questionAndAnswersDTOForAISuggestion = new QuestionAndAnswersDTOForAISuggestion();
			questionAndAnswersDTOForAISuggestion.setQuestion(elementLabelById.get(id));
			questionAndAnswersDTOForAISuggestion.setAnswers(responseByElementId.get(id));
			questionAndAnswersDTOForAISuggestionList.add(questionAndAnswersDTOForAISuggestion);
		}
		return questionAndAnswersDTOForAISuggestionList;
	}

	private RespondentUserProfileDetailsDTOForAISuggestion setProfileDetails(Long userId,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException {
		Map<String, Object> response = ExceptionUtil
				.throwExceptionsIfPresent(assessmentService.getRespondentUserDetails(userId, headers));
		if (!response.containsKey("respondentUserDTO")) {
			log.error("Respondent user details not found for fetching suggestion from AI.");
			throw new BussinessException(HttpStatus.NOT_FOUND,
					"Respondent user details not found for fetching suggestion from AI.");
		}
		RespondentDetailDTO respondentDetailDTO = (RespondentDetailDTO) response.get("respondentUserDTO");
		RespondentUserProfileDetailsDTOForAISuggestion profileDetails = new RespondentUserProfileDetailsDTOForAISuggestion();
		profileDetails.setDateOfBirth(respondentDetailDTO.getDateOfBirth());
		profileDetails.setHeightFt(respondentDetailDTO.getHeightFt());
		profileDetails.setHeightIn(respondentDetailDTO.getHeightIn());
		profileDetails.setWeight(respondentDetailDTO.getWeight());
		profileDetails.setWeightGrams(respondentDetailDTO.getWeightGrams());
		profileDetails.setMealPreference(respondentDetailDTO.getMealPreference());
		profileDetails.setMotherAlive(respondentDetailDTO.getMotherAlive());
		profileDetails.setMotherName(respondentDetailDTO.getMealPreference());
		profileDetails.setMotherDateOfBirth(respondentDetailDTO.getMotherDateOfBirth());
		profileDetails.setMotherWork(respondentDetailDTO.getMotherWork());
		profileDetails.setFatherAlive(respondentDetailDTO.getFatherAlive());
		profileDetails.setFatherName(respondentDetailDTO.getFatherName());
		profileDetails.setFatherDateOfBirth(respondentDetailDTO.getFatherDateOfBirth());
		profileDetails.setFatherWork(respondentDetailDTO.getFatherWork());
		profileDetails.setCorporateHealthPolicyFlag(respondentDetailDTO.getCorporateHealthPolicyFlag());
		profileDetails.setCorporateHealthPolicyName(respondentDetailDTO.getCorporateHealthPolicyName());
		profileDetails.setPersonalHealthPolicyFlag(respondentDetailDTO.getPersonalHealthPolicyFlag());
		profileDetails.setPersonalHealthPolicyName(respondentDetailDTO.getPersonalHealthPolicyName());

		setSvmValuesForSetProfileDetails(respondentDetailDTO, profileDetails);

		return profileDetails;
	}

	private void setSvmValuesForSetProfileDetails(RespondentDetailDTO respondentDetailDTO,
			RespondentUserProfileDetailsDTOForAISuggestion profileDetails)
			throws ContractException, BussinessException, TechnicalException {

		Set<String> svmIds = new HashSet<>();
		svmIds.add(respondentDetailDTO.getGender() != null ? respondentDetailDTO.getGender() : null);
		svmIds.add(respondentDetailDTO.getMaritalStatus() != null ? respondentDetailDTO.getMaritalStatus() : null);
		svmIds.add(respondentDetailDTO.getBloodGroup() != null ? respondentDetailDTO.getBloodGroup() : null);
		svmIds.add(respondentDetailDTO.getMotherGender() != null ? respondentDetailDTO.getMotherGender() : null);
		svmIds.add(
				respondentDetailDTO.getMotherBloodGroup() != null ? respondentDetailDTO.getMotherBloodGroup() : null);
		svmIds.add(respondentDetailDTO.getFatherGender() != null ? respondentDetailDTO.getFatherGender() : null);
		svmIds.add(
				respondentDetailDTO.getFatherBloodGroup() != null ? respondentDetailDTO.getFatherBloodGroup() : null);

//		Set.of(profileDetails.getGender(), profileDetails.getMaritalStatus(), profileDetails.getBloodGroup(),
//				profileDetails.getMotherGender(), profileDetails.getMotherBloodGroup(),
//				profileDetails.getFatherGender(), profileDetails.getFatherBloodGroup());

		List<String> corporateCoverageIds = null;
		if (CollectionUtils.isNotEmpty(respondentDetailDTO.getCorporateCoverageDTO())) {
			corporateCoverageIds = respondentDetailDTO.getCorporateCoverageDTO().stream()
					.map(a -> String.valueOf(a.getId())).toList();
			svmIds.addAll(corporateCoverageIds);
		}

		List<String> personalCoverageIds = null;
		if (CollectionUtils.isNotEmpty(respondentDetailDTO.getPersonalCoverageDTO())) {
			personalCoverageIds = respondentDetailDTO.getPersonalCoverageDTO().stream()
					.map(a -> String.valueOf(a.getId())).toList();
			svmIds.addAll(personalCoverageIds);
		}

		// removing any null object
		svmIds.remove(null);

		Map<Long, String> valueBySerialId = svmUtil.getValueBySerialIdMapBySerialIdIn(svmIds);

		profileDetails.setGender(respondentDetailDTO.getGender() != null
				? valueBySerialId.get(Long.valueOf(respondentDetailDTO.getGender()))
				: null);
		profileDetails.setMaritalStatus(respondentDetailDTO.getMaritalStatus() != null
				? valueBySerialId.get(Long.valueOf(respondentDetailDTO.getMaritalStatus()))
				: null);
		profileDetails.setBloodGroup(respondentDetailDTO.getBloodGroup() != null
				? valueBySerialId.get(Long.valueOf(respondentDetailDTO.getBloodGroup()))
				: null);
		profileDetails.setMotherGender(respondentDetailDTO.getMotherGender() != null
				? valueBySerialId.get(Long.valueOf(respondentDetailDTO.getMotherGender()))
				: null);
		profileDetails.setMotherBloodGroup(respondentDetailDTO.getMotherBloodGroup() != null
				? valueBySerialId.get(Long.valueOf(respondentDetailDTO.getMotherBloodGroup()))
				: null);
		profileDetails.setFatherGender(respondentDetailDTO.getFatherGender() != null
				? valueBySerialId.get(Long.valueOf(respondentDetailDTO.getFatherGender()))
				: null);
		profileDetails.setFatherBloodGroup(respondentDetailDTO.getFatherBloodGroup() != null
				? valueBySerialId.get(Long.valueOf(respondentDetailDTO.getFatherBloodGroup()))
				: null);

		setCorporateAndPersonalCoverageTo(corporateCoverageIds, personalCoverageIds, profileDetails, valueBySerialId);
	}

	private void setCorporateAndPersonalCoverageTo(List<String> corporateCoverageIds, List<String> personalCoverageIds,
			RespondentUserProfileDetailsDTOForAISuggestion profileDetails, Map<Long, String> valueBySerialId) {
		if (CollectionUtils.isNotEmpty(corporateCoverageIds)) {
			List<String> corporateHealthCoverageTo = new ArrayList<>();
			for (String corporateCoverageId : corporateCoverageIds) {
				corporateHealthCoverageTo.add(valueBySerialId.get(Long.valueOf(corporateCoverageId)));
			}
			profileDetails.setCorporateHealthCoverageTo(corporateHealthCoverageTo);
		}

		if (CollectionUtils.isNotEmpty(personalCoverageIds)) {
			List<String> personalHealthCoverageTo = new ArrayList<>();
			for (String personalCoverageId : personalCoverageIds) {
				personalHealthCoverageTo.add(valueBySerialId.get(Long.valueOf(personalCoverageId)));
			}
			profileDetails.setPersonalHealthCoverageTo(personalHealthCoverageTo);
		}
	}

	private void updateSectionWiseStatus(Long sectionId, Long assessmentId, Long userId, String status,
			int totalQuestionInOneSection, int responseCountInOneSection) {

		SectionWiseStatusMapping sectionWiseStatusMapping = sectionWiseStatusMappingRepository
				.findBysectionIdAndAssessmentIdAndUserId(sectionId, assessmentId, userId);

		if (sectionWiseStatusMapping == null) {
			sectionWiseStatusMapping = new SectionWiseStatusMapping();
			sectionWiseStatusMapping.setAssessmentId(assessmentId);
			sectionWiseStatusMapping.setSectionId(sectionId);
			sectionWiseStatusMapping.setUserId(userId);
			sectionWiseStatusMapping.setSectionResponseStatus(status);
			sectionWiseStatusMapping.setTotalQuestionInOneSection(totalQuestionInOneSection);
			sectionWiseStatusMapping.setResponseCountInOneSection(responseCountInOneSection);
			Section section = sectionRepository.findByIdAndActive(sectionId, Boolean.TRUE);
			sectionWiseStatusMapping.setSequence(section.getSequence());

			sectionWiseStatusMappingRepository.save(sectionWiseStatusMapping);
		} else {
			if (COMPLETED.equals(status)) {
				Long sequence = sectionWiseStatusMapping.getSequence();

				sectionWiseStatusMappingRepository.updateStatusBySequenceGreaterThanAndAssessmentIdAndUserId(sequence,
						assessmentId, userId);
			}
			sectionWiseStatusMapping.setSectionResponseStatus(status);
			sectionWiseStatusMapping.setTotalQuestionInOneSection(totalQuestionInOneSection);
			sectionWiseStatusMapping.setResponseCountInOneSection(responseCountInOneSection);
			sectionWiseStatusMappingRepository.save(sectionWiseStatusMapping);
		}
	}

	@Transactional
	private void updateTotalQuestionInOneAssessment(Long assessmentId, Long userId, Long totalQuestionInOneAssessment,
			Long totalResponseCountInOneAssessment) {
		AssessmentSubmittedResponseUser responseUser = assessmentSubmittedUserRepository
				.findByAssessmentIdAndUserId(assessmentId, userId);

		if (responseUser == null) {
			responseUser = new AssessmentSubmittedResponseUser();
			responseUser.setAssessmentId(assessmentId);
			responseUser.setUserId(userId);
		}

		responseUser.setTotalQuestionInOneAssessment(totalQuestionInOneAssessment);
		responseUser.setTotalResponseCountInOneAssessment(totalResponseCountInOneAssessment);
		assessmentSubmittedUserRepository.save(responseUser);
	}

	private void checkSectionComplete(AssessmentElementResponseDto assessmentElementResponseDto, Long userId,
			Double sectionTotal) throws TechnicalException, ContractException, BussinessException {
		log.info("Starting Async process => checkSectionComplete");
		Elements elements = elementRepository.findByIdAndActive(assessmentElementResponseDto.getElementId(), true);
		Section section = sectionRepository.findByIdAndActive(elements.getSectionId(), true);
		List<Elements> elementsList = elementRepository.findBySectionId(section.getId());
		SectionDto sectionDto = new SectionDto();
		BeanUtils.copyProperties(section, sectionDto);
		List<ElementsDto> elementsDto = new ArrayList<>();
		int responseCountInOneSection = 0;
		int mandatoryQuestionCount = 0;
		int nonMandatoryResponseCount = 0;
		int mandatoryResponseCompletedCount = 0;
		double sectionWeightage = 0;
		for (Elements element : elementsList) {

			ElementsDto elementDto = buildElementsDtoForAverageCalculation(element, userId,
					assessmentElementResponseDto.getAssessmentId());

			boolean isMandatory = Boolean.TRUE.equals(element.getMandatoryFlag());
			AssessmentElementResponseDto dto = elementDto.getResponse();

			if (isMandatory) {
				mandatoryQuestionCount++;
			}

			if (dto != null) {

				if (isMandatory) {
					if (dto.getResponses() != null && !dto.getResponses().isEmpty()) {
						mandatoryResponseCompletedCount++;
					}
				} else {
					nonMandatoryResponseCount++;

				}

				if ((Objects.equals(dto.getAssessmentResponseType(), AssessmentResponseType.SINGLE_CHOICE)
						|| Objects.equals(dto.getAssessmentResponseType(), AssessmentResponseType.MULTIPLE_CHOICE))
						&& (dto.getResponses() != null && !dto.getResponses().isEmpty())) {
					responseCountInOneSection++;
				}

				if (Objects.equals(dto.getAssessmentResponseType(), AssessmentResponseType.DOCUMENT_UPLOAD)
						&& dto.getDocumentDetailsDto() != null) {
					responseCountInOneSection++;
				}
			}
			elementsDto.add(elementDto);

		}
		sectionDto.setSectionElements(elementsDto);
		sectionDto.setTotalQuestionInOneSection(elementsList.size());
		sectionDto.setResponseCountInOneSection(responseCountInOneSection);

		if (mandatoryQuestionCount > 0 && responseCountInOneSection >= mandatoryQuestionCount) {
			sectionDto.setSectionStatus(COMPLETED);
		} else if (mandatoryQuestionCount == 0 && nonMandatoryResponseCount > 0
				&& nonMandatoryResponseCount == elementsList.size()) {
			sectionDto.setSectionStatus(COMPLETED);
		} else {
			sectionDto.setSectionStatus(PENDING);
		}

		BigDecimal secAverage = (responseCountInOneSection != 0) ? BigDecimal.valueOf(sectionTotal)
				.divide(BigDecimal.valueOf(responseCountInOneSection), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
		double sectionAverage = secAverage.doubleValue();
		long riskRatingId = calculateRiskRatingId(sectionAverage, sectionDto.getId(),
				assessmentElementResponseDto.getAssessmentId());

		if (COMPLETED.equalsIgnoreCase(sectionDto.getSectionStatus())) {
			SectionWiseRating sectionWiseRating = sectionWiseRatingRepository.findBySectionIdAndAssessmentIdAndUserId(
					sectionDto.getId(), assessmentElementResponseDto.getAssessmentId(), userId);

			if (org.springframework.util.ObjectUtils.isEmpty(sectionWiseRating)) {
				sectionWiseRating = SectionWiseRating.builder().sectionId(sectionDto.getId())
						.templateId(elements.getTemplateId())
						.assessmentId(assessmentElementResponseDto.getAssessmentId()).userId(userId)
						.riskRatingId(riskRatingId).createdOn(new Date()).active(true).sectionAverage(sectionAverage)
						.sectionTotal(sectionTotal).build();
			} else {
				sectionWiseRating.setSectionAverage(sectionAverage);
				sectionWiseRating.setRiskRatingId(riskRatingId);
				sectionWiseRating.setSectionTotal(sectionTotal);
				sectionWiseRating.setModifiedOn(new Date());
			}
			sectionWiseRatingRepository.save(sectionWiseRating);
		}
	}

	private long calculateRiskRatingId(double sectionAverage, Long sectionId, Long assessmentId) {
		long roundedAverage = Math.round(sectionAverage);

		SectionWiseRiskMatrix sectionWiseRiskMatrix = sectionWiseRiskMatrixRepository
				.findBysectionIdAndAssessmentIdAndRiskScore(sectionId, assessmentId, roundedAverage);

		if (org.apache.commons.lang3.ObjectUtils.isEmpty(sectionWiseRiskMatrix)) {
			sectionWiseRiskMatrix = sectionWiseRiskMatrixRepository
					.findBysectionIdAndAssessmentIdAndRiskScore(sectionId, null, roundedAverage);
		}

		if (sectionWiseRiskMatrix != null) {
			return sectionWiseRiskMatrix.getRiskRatingId();
		} else {
			return 0L;
		}
	}

	private ElementsDto buildElementsDtoForAverageCalculation(Elements element, Long userId, Long assessmentId)
			throws TechnicalException, NumberFormatException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		ElementsDto elementDto = new ElementsDto();

		BeanUtils.copyProperties(element, elementDto);

		List<ElementOptionsDto> masters = fetchElementOptions(element);
		elementDto.setValues(masters);

		List<ElementMasterValueDto> masterValues = fetchElementMasterValues(element);
		elementDto.setMasterSetValues(masterValues);

		AssessmentElementResponseDto assessmentElementResponseDto = fetchElementResponse(element, assessmentId, userId);
		elementDto.setResponse(assessmentElementResponseDto);

		if (element.getFamilyResponseFlag() == Boolean.TRUE) {
			RespondentDetails respondentDetails = respondentDetailsRepository.findByUserId(userId);

			if (respondentDetails != null) {

				RespondentDetailsDTO respondentDetailsDTO = buildRespondentDetailsDTO(respondentDetails);

				elementDto.setRespondentDetailsDTO(respondentDetailsDTO);
			}
		}

		log.info(LogUtil.exitLog(CLASSNAME));
		return elementDto;
	}

	private AssessmentElementResponse mapDtoToEntity(AssessmentElementResponseDto assessmentDto, Long userId)
			throws BussinessException, ContractException, TechnicalException {
		AssessmentElementResponse assessmentElementResponse = new AssessmentElementResponse();
		BeanUtils.copyProperties(assessmentDto, assessmentElementResponse);
		assessmentElementResponse.setCreatedBy(userId);
		assessmentElementResponse.setCreatedOn(new Date());

		if (Objects.equals(assessmentDto.getAssessmentResponseType(), AssessmentResponseType.SINGLE_CHOICE)
				|| Objects.equals(assessmentDto.getAssessmentResponseType(), AssessmentResponseType.MULTIPLE_CHOICE)) {

			assessmentElementResponse.setResponse(mergeStrings(assessmentDto.getResponses()));
		} else if (Objects.equals(assessmentDto.getAssessmentResponseType(), AssessmentResponseType.DOCUMENT_UPLOAD)
				&& (assessmentDto.getDocumentDetailsDto() != null)) {
			assessmentElementResponse.setResponse(processDocumentDetails(assessmentDto.getDocumentDetailsDto()));

		}

		assessmentElementResponse.setSpouseResponse(mergeStrings(assessmentDto.getSpouseResponse()));
		assessmentElementResponse.setMotherResponse(mergeStrings(assessmentDto.getMotherResponse()));
		assessmentElementResponse.setFatherResponse(mergeStrings(assessmentDto.getFatherResponse()));
		assessmentElementResponse.setMotherinlawResponse(mergeStrings(assessmentDto.getMotherinlawResponse()));
		assessmentElementResponse.setFatherinlawResponse(mergeStrings(assessmentDto.getFatherinlawResponse()));
		assessmentElementResponse.setChild1Response(mergeStrings(assessmentDto.getChild1Response()));
		assessmentElementResponse.setChild2Response(mergeStrings(assessmentDto.getChild2Response()));
		return assessmentElementResponse;
	}

	private Double calculateTotalPoints(List<Double> assessmentWeightage) {
		if (assessmentWeightage == null || assessmentWeightage.isEmpty()) {
			return 0.0;
		}

		Double totalPoints = 0.0;
		for (Double weightage : assessmentWeightage) {
			if (weightage != null) {
				totalPoints += weightage;
			}
		}

		return totalPoints;
	}

	private String mergeStrings(List<String> strings) {
		if (strings == null || strings.isEmpty()) {
			return null;
		}

		StringBuilder merged = new StringBuilder();
		for (String str : strings) {
			if (StringUtils.isNotEmpty(str)) {
				if (merged.length() > 0) {
					merged.append(",");
				}
				merged.append(str);
			}
		}

		return merged.toString();
	}

	private String processDocumentDetails(List<DocumentGetDto> documentDetails)
			throws BussinessException, ContractException, TechnicalException {

		String allMergedDocuments = null;

		try {
			if (documentDetails == null) {
				return allMergedDocuments;
			}

			for (DocumentGetDto document : documentDetails) {
				if (document != null && ObjectUtils.isZero(document.getId())) {
					String[] documentNameParts = document.getDocumentName().split("\\.");
					if (documentNameParts.length < 2) {
						log.warn("Invalid document name: {}", document.getDocumentName());
						continue;
					}

					DocumentDto documentDto = new DocumentDto();
					documentDto.setFileName(documentNameParts[0]);
					documentDto.setExtension(documentNameParts[1]);
					documentDto.setBase64(document.getBase64());
					documentDto.setSize("1");

					Long docId = documentUtil.addDocument(documentDto);

					allMergedDocuments = StringUtils.isNotEmpty(allMergedDocuments)
							? String.join(",", allMergedDocuments, String.valueOf(docId))
							: String.valueOf(docId);

				}
			}

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return allMergedDocuments;
	}

	private String formatDate(Date date) {
		return date != null ? org.apache.commons.lang3.time.DateFormatUtils.format(date, DATE_FORMAT) : null;
	}

}
