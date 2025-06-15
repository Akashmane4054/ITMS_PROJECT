package com.ehr.assessment.business.serviceimpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.ehr.assessment.business.dto.AssessmentCountsDTO;
import com.ehr.assessment.business.dto.AssessmentDto;
import com.ehr.assessment.business.dto.AssessmentInsightsDetailsDTO;
import com.ehr.assessment.business.dto.AssessmentInsightsDto;
import com.ehr.assessment.business.dto.CoverageDTO;
import com.ehr.assessment.business.dto.ExternalProjectionDTO;
import com.ehr.assessment.business.dto.PlayPauseAssessmentDTO;
import com.ehr.assessment.business.dto.PolicyDTO;
import com.ehr.assessment.business.dto.RespondentDetailDTO;
import com.ehr.assessment.business.dto.RespondentDetailsDTO;
import com.ehr.assessment.business.dto.RespondentUserDTO;
import com.ehr.assessment.business.dto.ScheduleAssessmentDTO;
import com.ehr.assessment.business.dto.SectionDto;
import com.ehr.assessment.business.dto.TemplateMappingDTO;
import com.ehr.assessment.business.enums.AssessmentProgress;
import com.ehr.assessment.business.enums.AssessmentRemainder;
import com.ehr.assessment.business.enums.AssessmentResponseStatus;
import com.ehr.assessment.business.enums.AssessmentStatus;
import com.ehr.assessment.business.enums.PolicyType;
import com.ehr.assessment.business.service.AssessmentService;
import com.ehr.assessment.business.validation.AssessmentServiceValidator;
import com.ehr.assessment.business.validation.ExcelValidator;
import com.ehr.assessment.integration.domain.Assessment;
import com.ehr.assessment.integration.domain.AssessmentSubmittedResponseUser;
import com.ehr.assessment.integration.domain.AssessmentUserMapping;
import com.ehr.assessment.integration.domain.RespondentCoverageInfoMapping;
import com.ehr.assessment.integration.domain.RespondentDetails;
import com.ehr.assessment.integration.domain.Section;
import com.ehr.assessment.integration.domain.SectionWiseRating;
import com.ehr.assessment.integration.domain.TemplateMaster;
import com.ehr.assessment.integration.repository.AssessmentRepository;
import com.ehr.assessment.integration.repository.AssessmentSubmittedUserRepository;
import com.ehr.assessment.integration.repository.AssessmentUserRepository;
import com.ehr.assessment.integration.repository.ElementsRepository;
import com.ehr.assessment.integration.repository.RespondentCoverageInfoMappingRepository;
import com.ehr.assessment.integration.repository.RespondentDetailsRepository;
import com.ehr.assessment.integration.repository.SectionRepository;
import com.ehr.assessment.integration.repository.SectionWiseRatingRepository;
import com.ehr.assessment.integration.repository.SectionWiseStatusMappingRepository;
import com.ehr.assessment.integration.repository.TemplateRepository;
import com.ehr.assessment.presentation.util.CustomTransactional;
import com.ehr.assessment.presentation.util.Paginate;
import com.ehr.core.dto.DocumentDto;
import com.ehr.core.dto.DocumentGetDto;
import com.ehr.core.dto.ListingDto;
import com.ehr.core.dto.NotificationRequest;
import com.ehr.core.dto.SuccessResponse;
import com.ehr.core.dto.UserMasterDTO;
import com.ehr.core.enums.NotificationType;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.feignclients.DocumentServiceFeignProxy;
import com.ehr.core.feignclients.HealthCompanyServiceFeignProxy;
import com.ehr.core.util.CSVValidatorUtil;
import com.ehr.core.util.Constants;
import com.ehr.core.util.DateUtil;
import com.ehr.core.util.DocumentUtil;
import com.ehr.core.util.EmailUtil;
import com.ehr.core.util.ExceptionUtil;
import com.ehr.core.util.FileManager;
import com.ehr.core.util.FileSize;
import com.ehr.core.util.FileValidatorUtil;
import com.ehr.core.util.LogUtil;
import com.ehr.core.util.ObjectUtils;
import com.ehr.core.util.PropertyUtils;
import com.ehr.core.util.SvmUtil;
import com.ehr.core.util.UserUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {
	private static final String USER_ID = "userId";

	private static final String YYYY_MM_DD = "yyyy-MM-dd";

	private static final String CLASSNAME = AssessmentServiceImpl.class.getSimpleName();

	private static final String ASSESSMENT_ID = "assessmentId";
	private static final String TITLE = "TITLE";
	private static final String GENDER = "GENDER";
	private static final String MARITAL_STATUS = "MARITAL_STATUS";
	private static final String BLOOD_GROUP = "BLOOD_GROUP";
	private static final String WORK_STATUS = "WORK_STATUS";
	private static final String MEAL_PREFERENCE = "MEAL_PREFERENCE";
	private static final String CHILD_PLANNING = "CHILD_PLANNING";
	private static final String ASSESSMENT_NOT_AVAILABLE = "Assessment not available.";
	private static final String NO_SUCH_ASSESSMENT_EXIST = "no such assessment exist";
	private static final String PROCESS = "$process";
	private static final String NAME = "$name";
	private static final String TRIGGERING_WORKFLOW_EMAIL_PROCESS_START = "triggering workflow email process start";
	public static final String DATE_FORMAT = "dd-MM-yyyy";
	private static final String TIMEZONE_IS = "Timezone is  ";
	private static final String PATH = "json/ColumnNamesByListingType.json";

	@PersistenceContext
	private final EntityManager em;

	private final UserUtil userUtil;
	private final AssessmentUserRepository assessmentUserMappingRepository;
	private final AssessmentRepository assessmentRepository;
	private final EmailUtil emailUtil;
	private final HealthCompanyServiceFeignProxy companyServiceFeignProxy;
	private final TemplateRepository templateRepository;
	private final DocumentServiceFeignProxy documentServiceFeignProxy;
	private final RespondentDetailsRepository respondentDetailsRepository;
	private final AssessmentResponseServiceImpl assessmentResponseServiceImpl;
	private final SvmUtil svmUtil;
	private final DocumentUtil documentUtil;
	private final RespondentCoverageInfoMappingRepository coverageInfoMappingRepository;
	private final SectionWiseRatingRepository sectionWiseRatingRepository;
	private final SectionRepository sectionRepository;
	private final ElementsRepository elementsRepository;
	private final SectionWiseStatusMappingRepository sectionWiseStatusMappingRepository;
	private final AssessmentSubmittedUserRepository assessmentSubmittedUserRepository;

	@Lazy
	private final AssessmentSchedulerService assessmentSchedulerService;

	@Value("${fileManager.ClassName}")
	private String fileManagerClassName;

	@Value("${document.docFolder}")
	private String docFolder;

	@Value("${staticDocExcelPath}")
	private String staticDocExcelPath;

	@Value("${excelPath}")
	private String excelPath;

	private FileManager fileManager;

	@Autowired
	public void setService(ApplicationContext context) {
		fileManager = (FileManager) context.getBean(fileManagerClassName);
	}

	@Override
	@Transactional
	public Map<String, Object> saveAssessment(AssessmentDto assessmentDto, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {

		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> response = new HashMap<>();

		Map<String, Object> userResponse = verifyToken(headers);
		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
		String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));
		Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));
		Assessment assessment = null;
		try {
			AssessmentServiceValidator.dtoValidationUtil(assessmentDto, language);
			assessment = assessmentRepository.findByIdAndCompanyId(assessmentDto.getId(), companyId);
			if (assessment != null) {
				uniqueNameCheck(Boolean.TRUE, assessmentDto, companyId);
				validateAssessmentStatusForSave(assessment, userId);
				PropertyUtils.copyNonNullProperties(assessmentDto, assessment);
				assessment.setModifiedBy(userId);
				assessment.setCompanyId(companyId);
				assessment.setModifiedOn(new Date());
				assessment.setProgressStatus(AssessmentProgress.BASIC_INFO);
				assessment = assessmentRepository.save(assessment);
			} else {
				assessment = new Assessment();
				uniqueNameCheck(Boolean.FALSE, assessmentDto, companyId);
				BeanUtils.copyProperties(assessmentDto, assessment);
				assessment.setCompanyId(companyId);
				assessment.setCreatedBy(userId);
				assessment.setCreatedOn(new Date());
				if (assessmentDto.getSelfReview() != null && assessmentDto.getSelfReview().equals(Boolean.TRUE)) {
					assessment.setReviewer(assessmentDto.getOwner());
				} else {
					assessment.setReviewer(assessmentDto.getReviewer());
				}
				assessment.setFinancialYear(calculateFinancialYear());
				assessment.setStatus(AssessmentStatus.DRAFT);
				assessment.setProgressStatus(AssessmentProgress.BASIC_INFO);
				assessment = assessmentRepository.save(assessment);

			}

			String message = (assessmentDto.getId() != null && assessmentDto.getId() > 0)
					? "Assessment Updated Successfully"
					: "Assessment Saved Successfully";
			response.put(ASSESSMENT_ID, assessment.getId() != null ? assessment.getId() : null);
			response.put(Constants.SUCCESS, new SuccessResponse(message));
			response.put(Constants.ERROR, null);

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	public void validateAssessmentStatusForSave(Assessment assessment, Long userId) throws BussinessException {
		boolean isUneditableStatus = Boolean.FALSE.equals(assessment.getActive())
				|| AssessmentStatus.ACTIVE.equals(assessment.getStatus())
				|| AssessmentStatus.CLOSED.equals(assessment.getStatus())
				|| (AssessmentStatus.FOR_REVIEW.equals(assessment.getStatus())
						&& !assessment.getReviewer().equals(userId));

		if (isUneditableStatus) {
			String statusName = Boolean.FALSE.equals(assessment.getActive()) ? "INACTIVE"
					: assessment.getStatus().getStatus();

			throw new BussinessException(HttpStatus.BAD_REQUEST,
					"Assessment cannot be edited. Status is " + statusName + ".");
		}
	}

	public void uniqueNameCheck(Boolean isEdit, AssessmentDto assessmentDto, Long companyId) throws ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		Assessment assessment = assessmentRepository.findByNameAndCompanyIdAndActive(assessmentDto.getName(), companyId,
				Boolean.TRUE);
		if (isEdit.equals(Boolean.TRUE)) {
			if (assessment != null && !assessment.getId().equals(assessmentDto.getId())) {
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
						"Name : " + assessmentDto.getName() + ", Already Exist");
			}
		} else {
			if (assessment != null) {
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
						"Name : " + assessmentDto.getName() + ", Already Exist");
			}
		}
		log.info(LogUtil.exitLog(CLASSNAME));
	}

	private void sendNotificationEmailUser(Assessment assessment, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException {
		try {
			if (assessment.getCreatedBy() == null) {
				log.info(LogUtil.exitLog(CLASSNAME));
				return;
			}

			Date expiredOn = assessment.getExpiredOn();

			SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");

			String endDate = dateFormat.format(expiredOn);

			String userName = userUtil.getUserFullName((assessment.getCreatedBy()));

			if (userName != null) {
				UserMasterDTO user = userUtil.getUserById(assessment.getReviewer(), headers);
				if (user != null && StringUtils.isNotEmpty(user.getEmailAddress())
						&& StringUtils.isNotEmpty(user.getUserFirstName())) {
					sendWorkFlowEmailForUser("EBITC009", assessment.getName(), user.getEmailAddress(), userName,
							endDate, user.getUserFirstName());
					log.info("Triggered email sending for Assessment For Review User : {}", user.getUserFirstName());
				}
			}

		} catch (Exception e) {
			log.error("Error in sending notifications");
		}
		log.info(LogUtil.exitLog(CLASSNAME));
	}

	private void setAssessmentDisplayId(Long companyId, Long id, Assessment assessment,
			MultiValueMap<String, String> headers) throws BussinessException, ContractException, TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));
		try {

			Map<String, Object> companyDetails = companyServiceFeignProxy.findCompanyNameById(companyId, headers);

			String companyName = (String) companyDetails.get(Constants.COMPANY_NAME);

			String assessmentDisplayId = generateAssessmentDisplayId(companyName, id, companyId);

			assessment.setAssessmentDisplayId(assessmentDisplayId);

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			log.error(Constants.ERROR_LOG, e);
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, Constants.SPACE);
		}
		log.info(LogUtil.exitLog(CLASSNAME));

	}

	private String generateAssessmentDisplayId(String companyName, Long id, Long companyId) throws BussinessException {

		log.info(LogUtil.startLog(CLASSNAME));
		companyName = companyName.substring(0, 3).toUpperCase();
		String assessmentDisplayId = null;

		log.info(Constants.DB_CALL + "FIND_assessment_DETAILS_BY_assessment_ID", +id);
		Optional<Assessment> assessmentoptional = assessmentRepository.findById(id);
		Assessment assessment = assessmentoptional.orElse(null);

		if (assessment == null) {
			log.error("assessment not found with assessmentId : " + id);
			throw new BussinessException(HttpStatus.EXPECTATION_FAILED, "assessment not found");
		}

		// Get current financial year
		String financialYear = calculateFinancialYear();

		log.info(Constants.DB_CALL + "find Max Assessment Display Id Sequence in Assessment");
		Long maxAssessementDisplayIdSequence = assessmentRepository.findMaxAssessmentDisplayIdSequence(companyId,
				financialYear);

		if (maxAssessementDisplayIdSequence != null) {

			assessment.setAssessmentDisplayIdSequence(maxAssessementDisplayIdSequence + 1l);
			log.info(Constants.DB_CALL + "find Assessment Display Id By Assessment Display Id Sequence : {}",
					maxAssessementDisplayIdSequence);

			String displayId = assessmentRepository
					.findAssessmentDisplayIdByAssessmentDisplayIdSequence(maxAssessementDisplayIdSequence, companyId);

			Long numberPart = null;
			if (!StringUtils.isEmpty(displayId)) {
				String[] splitArray = displayId.split("/");
				numberPart = Long.parseLong(String.valueOf(extractNumberPart(splitArray)));
			} else {
				numberPart = maxAssessementDisplayIdSequence;
			}

			numberPart += 1;
			String newNumberPart = String.format("%07d", numberPart);
			assessmentDisplayId = companyName + "/" + "ASS" + "/" + financialYear + "/" + newNumberPart;
		} else {

			assessment.setAssessmentDisplayIdSequence(1l);
			assessmentDisplayId = companyName + "/" + "ASS" + "/" + financialYear + "/" + "0000001";
		}

		log.info(LogUtil.exitLog(CLASSNAME));
		return assessmentDisplayId;
	}

	private String calculateFinancialYear() {
		log.info(LogUtil.startLog(CLASSNAME));
		int year = Integer.parseInt(DateFormatUtils.format(new Date(), "yy"));
		if (LocalDate.now().isBefore(LocalDate.of(year, Month.APRIL, 1))) {
			return (year - 1) + "-" + year;
		} else {
			return year + "-" + (year + 1);
		}
	}

	private int extractNumberPart(String[] splitArray) {
		log.info(LogUtil.startLog(CLASSNAME));
		if (splitArray.length >= 3) {
			return Integer.parseInt(splitArray[splitArray.length - 1]);
		} else {
			throw new IllegalArgumentException("Unexpected display code format");
		}
	}

	public void sendWorkFlowEmailForUser(String templateCode, String process, String emailAddress, String userName,
			String endDate, String name) throws BussinessException, ContractException, TechnicalException {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(() -> {
			try {
				log.info(TRIGGERING_WORKFLOW_EMAIL_PROCESS_START);
				NotificationRequest notificationRequest = new NotificationRequest();
				notificationRequest.setEmailAttachments(new ArrayList<>());
				notificationRequest.setRequestType(NotificationType.EMAIL_NOTIFICATION.getId());
				notificationRequest.setTemplateCode(templateCode);
				Map<String, String[]> senderReciever = new HashMap<>();
				String[] userEmailAddress = { emailAddress };
				senderReciever.put("to", userEmailAddress);
				notificationRequest.setSenderReciever(senderReciever);
				Map<String, String> templateVariables = new HashMap<>();
				templateVariables.put(NAME, name);
				templateVariables.put(PROCESS, process);
				templateVariables.put("$createdBy", userName);
				templateVariables.put("$enddate", endDate);
				notificationRequest.setTemplateVariables(templateVariables);
				emailUtil.sendNotification(notificationRequest);
			} catch (Exception | BussinessException | ContractException | TechnicalException e) {
				log.error("Error while sending the notification to user {}", emailAddress, e);
			}

		});
		executorService.shutdown();
	}

	@Override
	public Map<String, Object> getAssessmentUsers(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		verifyToken(headers);
		Map<String, Object> response = new HashMap<>();
		try {
			List<Long> assessmentUserIds = assessmentUserMappingRepository.findByAssessmentId(assessmentId);
			List<Long> userIds = assessmentUserIds.stream().filter(ObjectUtils::isPositiveNonZero)
					.collect(Collectors.toList());

			List<UserMasterDTO> userlist = userUtil.getUserDetailsByUserIds(userIds, headers);
			response.put("assessmentUsers", userlist);
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	@Override
	public void sendReminders() throws BussinessException, ContractException, TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));

		LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
		Date oneMinuteAgoDate = Date.from(oneMinuteAgo.atZone(ZoneId.systemDefault()).toInstant());

		List<AssessmentUserMapping> assessmentUserMappings = assessmentUserMappingRepository
				.findByAssessmentResponseStatusNotAndLastReminderSentBefore(AssessmentResponseStatus.COMPLETED,
						oneMinuteAgoDate);

		for (AssessmentUserMapping assessmentUserMapping : assessmentUserMappings) {

			Optional<Assessment> assessmentOptional = assessmentRepository
					.findById(assessmentUserMapping.getAssessmentId());
			if (assessmentOptional.isEmpty())
				continue;

			Assessment assessment = assessmentOptional.get();

			ExternalProjectionDTO dto = respondentDetailsRepository
					.findDetailsByUserId(assessmentUserMapping.getUserId());

			Date dueDate = assessment.getExpiredOn();
			Date publishDate = assessment.getPublishedOn() != null ? assessment.getPublishedOn() : new Date();

			if (dueDate != null && dueDate.after(oneMinuteAgoDate)) {

				LocalDateTime publishDateTime = publishDate.toInstant().atZone(ZoneId.systemDefault())
						.toLocalDateTime();
				Date threeDaysAfterPublish = Date
						.from(publishDateTime.plusDays(3).atZone(ZoneId.systemDefault()).toInstant());
				Date sevenDaysAfterPublish = Date
						.from(publishDateTime.plusDays(7).atZone(ZoneId.systemDefault()).toInstant());

				// Send 3-day reminder
				if (oneMinuteAgoDate.after(threeDaysAfterPublish)
						&& AssessmentRemainder.FIRST.equals(assessmentUserMapping.getEmailReminder())) {
					sendWorkFlowEmailReminder("EHRREM1", dto.getEmailAddress(), dto.getFullName());
					assessmentUserMapping.setEmailReminder(AssessmentRemainder.SECOND);
					assessmentUserMapping.setLastReminderSent(oneMinuteAgoDate);
					assessmentUserMappingRepository.save(assessmentUserMapping);
				}

				// Send 7-day reminder
				if (oneMinuteAgoDate.after(sevenDaysAfterPublish)
						&& AssessmentRemainder.SECOND.equals(assessmentUserMapping.getEmailReminder())) {
					sendWorkFlowEmailReminder("EHRREM2", dto.getEmailAddress(), dto.getFullName());
					assessmentUserMapping.setEmailReminder(AssessmentRemainder.FINAL);
					assessmentUserMapping.setLastReminderSent(oneMinuteAgoDate);
					assessmentUserMappingRepository.save(assessmentUserMapping);
				}
			}
		}

		log.info(LogUtil.exitLog(CLASSNAME));
	}

	public void sendWorkFlowEmailReminder(String templateCode, String emailAddress, String userName)
			throws BussinessException, ContractException, TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));
		log.info(TRIGGERING_WORKFLOW_EMAIL_PROCESS_START);
		NotificationRequest notificationRequest = new NotificationRequest();
		notificationRequest.setEmailAttachments(new ArrayList<>());
		notificationRequest.setRequestType(NotificationType.EMAIL_NOTIFICATION.getId());
		notificationRequest.setTemplateCode(templateCode);
		Map<String, String[]> senderReciever = new HashMap<>();
		String[] userEmailAddress = { emailAddress };
		senderReciever.put("to", userEmailAddress);
		notificationRequest.setSenderReciever(senderReciever);
		Map<String, String> templateVariables = new HashMap<>();
		templateVariables.put(NAME, userName);

		notificationRequest.setTemplateVariables(templateVariables);
		emailUtil.sendAsyncNotification(notificationRequest);
	}

	@Override
	public Map<String, Object> deleteAssessment(Long assessmentId, Boolean status,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> userResponse = verifyToken(headers);
		Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));
		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));

		Map<String, Object> map = new HashMap<>();
		try {
			Assessment assessment = assessmentRepository.findByIdAndCompanyId(assessmentId, companyId);
			if (assessment == null)
				throw new BussinessException(HttpStatus.NOT_FOUND, ASSESSMENT_NOT_AVAILABLE);

			assessment.setActive(status);
			assessment.setModifiedOn(new Date());
			assessment.setModifiedBy(userId);
			assessment = assessmentRepository.save(assessment);

			String successMessage = Boolean.TRUE.equals(status) ? "Assessment activated successfully"
					: "Assessment deactivated successfully";
			map.put(Constants.SUCCESS, new SuccessResponse(successMessage));
			map.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;

	}

	@Override
	public Map<String, Object> findAllAssessmentPagination(ListingDto listingDto, Long assessmentStatus,
			MultiValueMap<String, String> headers) throws BussinessException, TechnicalException, ContractException {
		Map<String, Object> map = new HashMap<>();
		log.info(LogUtil.startLog(CLASSNAME));

		Map<String, Object> userResponse = ExceptionUtil.throwExceptionsIfPresent(userUtil.tokenVerification(headers));

		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
		Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

		try {

			if (!ObjectUtils.isNotNonZero(listingDto.getLength())) {
				listingDto.setLength(Constants.ONE_BILLION);
			}

			Page<Assessment> list = Paginate.paginate(em, listingDto, Assessment.class, companyId, userId,
					assessmentStatus);

			log.info("Retrieved assessments: {}", list.getContent());

			List<Assessment> domainList = list.getContent();
			List<AssessmentDto> dtoList = new ArrayList<>();

			Long totalCount = list.getTotalElements();

			Set<Long> userIds = new HashSet<>();

			if (CollectionUtils.isNotEmpty(domainList)) {
				log.info("newlist size => {}", domainList.size());
				for (Assessment assessment : domainList) {
					if (ObjectUtils.isPositiveNonZero(assessment.getCreatedBy())) {
						userIds.add(assessment.getCreatedBy());
					}
					if (ObjectUtils.isPositiveNonZero(assessment.getModifiedBy())) {
						userIds.add(assessment.getModifiedBy());
					}
				}
			}
			Map<String, UserMasterDTO> userDtos = userUtil.getLoggedInUserByIds(userIds, headers);

			AssessmentStatus status = null;
			for (AssessmentStatus s : AssessmentStatus.values()) {
				if (s.getStatusCode() == assessmentStatus.intValue()) {
					status = s;
					break;
				}
			}

			if (status != null) {
				switch (status) {
				case DRAFT:
					log.info("Processing DRAFT assessments.");
					for (Assessment domain : domainList) {
						if (Objects.equals(domain.getStatus(), AssessmentStatus.DRAFT)) {
							AssessmentDto dto = new AssessmentDto();
							BeanUtils.copyProperties(domain, dto);
							dto.setStatusValue(status);
							dto.setTemplateType(getTemplateType(domain.getTemplateId()));
							dto.setCreatedOn(formatDate(domain.getCreatedOn()));
							dto.setModifiedOn(formatDate(domain.getModifiedOn()));
							dto.setExpiredOn(formatDate(domain.getExpiredOn()));
							dto.setCreatedBy(Paginate.getFullName(userDtos, domain.getCreatedBy()));
							dto.setModifiedBy(Paginate.getFullName(userDtos, domain.getModifiedBy()));
							addAssessmentInsights(dto, domain);

							dtoList.add(dto);
						}
					}
					break;

				case FOR_REVIEW:
					log.info("Processing FOR_REVIEW assessments.");
					for (Assessment domain : domainList) {
						if (Objects.equals(domain.getStatus(), AssessmentStatus.FOR_REVIEW)) {
							AssessmentDto dto = new AssessmentDto();
							BeanUtils.copyProperties(domain, dto);
							dto.setStatusValue(status);
							dto.setTemplateType(getTemplateType(domain.getTemplateId()));
							dto.setCreatedOn(formatDate(domain.getCreatedOn()));
							dto.setModifiedOn(formatDate(domain.getModifiedOn()));
							dto.setExpiredOn(formatDate(domain.getExpiredOn()));
							dto.setCreatedBy(Paginate.getFullName(userDtos, domain.getCreatedBy()));
							dto.setModifiedBy(Paginate.getFullName(userDtos, domain.getModifiedBy()));
							addAssessmentInsights(dto, domain);

							dtoList.add(dto);
						}
					}
					break;

				case SCHEDULED:
					log.info("Processing SCHEDULED assessments.");
					for (Assessment domain : domainList) {
						if (Objects.equals(domain.getStatus(), AssessmentStatus.SCHEDULED)) {
							AssessmentDto dto = new AssessmentDto();
							BeanUtils.copyProperties(domain, dto);
							dto.setStatusValue(status);
							dto.setTemplateType(getTemplateType(domain.getTemplateId()));
							dto.setCreatedOn(formatDate(domain.getCreatedOn()));
							dto.setModifiedOn(formatDate(domain.getModifiedOn()));
							dto.setExpiredOn(formatDate(domain.getExpiredOn()));
							dto.setCreatedBy(Paginate.getFullName(userDtos, domain.getCreatedBy()));
							dto.setModifiedBy(Paginate.getFullName(userDtos, domain.getModifiedBy()));
							addAssessmentInsights(dto, domain);

							dtoList.add(dto);
						}
					}
					break;

				case ACTIVE:
					log.info("Processing ACTIVE assessments.");
					for (Assessment domain : domainList) {
						if (Objects.equals(domain.getStatus(), AssessmentStatus.ACTIVE)) {
							AssessmentDto dto = new AssessmentDto();
							BeanUtils.copyProperties(domain, dto);
							dto.setStatusValue(status);
							dto.setTemplateType(getTemplateType(domain.getTemplateId()));
							dto.setCreatedOn(formatDate(domain.getCreatedOn()));
							dto.setModifiedOn(formatDate(domain.getModifiedOn()));
							dto.setExpiredOn(formatDate(domain.getExpiredOn()));
							dto.setCreatedBy(Paginate.getFullName(userDtos, domain.getCreatedBy()));
							dto.setModifiedBy(Paginate.getFullName(userDtos, domain.getModifiedBy()));
							addAssessmentInsights(dto, domain);

							dtoList.add(dto);
						}
					}
					break;

				case CLOSED:
					log.info("Processing CLOSED assessments.");
					for (Assessment domain : domainList) {
						if (Objects.equals(domain.getStatus(), AssessmentStatus.CLOSED)) {
							AssessmentDto dto = new AssessmentDto();
							BeanUtils.copyProperties(domain, dto);
							dto.setStatusValue(status);
							dto.setTemplateType(getTemplateType(domain.getTemplateId()));
							dto.setCreatedOn(formatDate(domain.getCreatedOn()));
							dto.setModifiedOn(formatDate(domain.getModifiedOn()));
							dto.setExpiredOn(formatDate(domain.getExpiredOn()));
							dto.setCreatedBy(Paginate.getFullName(userDtos, domain.getCreatedBy()));
							dto.setModifiedBy(Paginate.getFullName(userDtos, domain.getModifiedBy()));
							addAssessmentInsights(dto, domain);

							dtoList.add(dto);
						}
					}
					break;

				default:
					log.warn("Unhandled assessment status: {}", assessmentStatus);
					break;
				}

			} else {
				log.info("No matching status found. Returning all data.");
				for (Assessment domain : domainList) {
					AssessmentDto dto = new AssessmentDto();
					BeanUtils.copyProperties(domain, dto);
					dto.setStatusValue(domain.getStatus());
					dto.setTemplateType(getTemplateType(domain.getTemplateId()));
					dto.setCreatedOn(formatDate(domain.getCreatedOn()));
					dto.setModifiedOn(formatDate(domain.getModifiedOn()));
					dto.setExpiredOn(formatDate(domain.getExpiredOn()));
					dto.setCreatedBy(Paginate.getFullName(userDtos, domain.getCreatedBy()));
					dto.setModifiedBy(Paginate.getFullName(userDtos, domain.getModifiedBy()));
					addAssessmentInsights(dto, domain);

					dtoList.add(dto);
				}
			}

			map.put(Constants.DTO_LIST, dtoList);
			map.put(Constants.TOTAL_COUNT, CollectionUtils.isNotEmpty(domainList) ? totalCount : 0);
			map.put(Constants.SORT, listingDto.getSort());
			map.put(Constants.SEARCH, listingDto.getSearch());
			map.put(Constants.DEFAULT_SORT, listingDto.getDefaultSort());
			map.put(Constants.ERROR, null);
			map.put(Constants.SUCCESS, new SuccessResponse(Constants.LISTED_SUCCESSFULLY));

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			log.error(Constants.ERROR_LOG, e);
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, Constants.SPACE);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	private Integer getTemplateType(Long templateId) {
		TemplateMaster templateMaster = templateRepository.findByIdAndActive(templateId, Boolean.TRUE);
		return templateMaster != null ? templateMaster.getTemplateType().getId() : null;
	}

	private String formatDate(Date date) {
		return date != null ? org.apache.commons.lang3.time.DateFormatUtils.format(date, DATE_FORMAT) : null;
	}

	private void addAssessmentInsights(AssessmentDto dto, Assessment domain) {

		AssessmentInsightsDto insightsDto = new AssessmentInsightsDto();

		List<AssessmentUserMapping> assessmentUserMapping = assessmentUserMappingRepository
				.findByAssessmentIds(domain.getId());

		if (assessmentUserMapping != null && !assessmentUserMapping.isEmpty()) {

			int noOfRespondent = assessmentUserMapping.size();

			int noOfResponseSubmitted = (int) assessmentUserMapping.stream()
					.filter(mapping -> mapping.getAssessmentResponseStatus() != null
							&& mapping.getAssessmentResponseStatus().equals(AssessmentResponseStatus.COMPLETED))
					.count();

			int noOfResponseNotSubmitted = noOfRespondent - noOfResponseSubmitted;

			int noOfResponseSubmittedPercent = (int) ((noOfResponseSubmitted * 100.0) / noOfRespondent);

			insightsDto.setNoOfRespondent(noOfRespondent);
			insightsDto.setNoOfResponseSubmitted(noOfResponseSubmitted);
			insightsDto.setNoOfResponseNotSubmitted(noOfResponseNotSubmitted);
			insightsDto.setNoOfResponseSubmittedPercent(noOfResponseSubmittedPercent);

		} else {

			insightsDto.setNoOfRespondent(0);
			insightsDto.setNoOfResponseSubmitted(0);
			insightsDto.setNoOfResponseNotSubmitted(0);
			insightsDto.setNoOfResponseSubmittedPercent(0);
		}
		dto.setAssessmentInsightsDto(insightsDto);
	}

	@Override
	public Map<String, Object> saveAssessmentTemplateMapping(TemplateMappingDTO mappingDTO,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();

		Map<String, Object> userResponse = verifyToken(headers);
		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
		Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));
		try {

			if (ObjectUtils.isPositiveNonZero(mappingDTO.getTemplateId())) {
				TemplateMaster activeCheck = templateRepository.findByIdAndActive(mappingDTO.getTemplateId(),
						Boolean.TRUE);
				if (activeCheck == null) {
					throw new BussinessException(HttpStatus.BAD_REQUEST,
							"Selected Template is inactive Kindly select new Template");
				}
			}

			Assessment assessment = assessmentRepository.findByIdAndCompanyId(mappingDTO.getAssessmentId(), companyId);
			if (assessment != null) {
				validateAssessmentStatusForSave(assessment, userId);
				assessment.setTemplateId(mappingDTO.getTemplateId());
				assessment.setModifiedBy(userId);
				assessment.setModifiedOn(new Date());
				assessment.setProgressStatus(AssessmentProgress.TEMPLATE);
				assessmentRepository.save(assessment);
			} else {
				throw new BussinessException(HttpStatus.NOT_FOUND, NO_SUCH_ASSESSMENT_EXIST);
			}

			map.put(ASSESSMENT_ID, assessment.getId() != null ? assessment.getId() : null);
			map.put(Constants.SUCCESS, new SuccessResponse("Template Mapped Successfully!"));
			map.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			log.error(Constants.ERROR_LOG, e);
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, Constants.SPACE);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	@Override
	public Map<String, Object> assessmentProgressMapping(TemplateMappingDTO mappingDTO,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> userResponse = verifyToken(headers);
		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
		Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));
		try {
			Assessment assessment = assessmentRepository.findByIdAndCompanyId(mappingDTO.getAssessmentId(), companyId);
			if (assessment != null) {
				assessment.setProgressStatus(mappingDTO.getProgressStatus());
				assessment.setModifiedBy(userId);
				assessment.setModifiedOn(new Date());
				assessmentRepository.save(assessment);
			} else {
				throw new BussinessException(HttpStatus.NOT_FOUND, NO_SUCH_ASSESSMENT_EXIST);
			}

			map.put(Constants.SUCCESS, new SuccessResponse("Assessment Progress Updated Successfully!"));
			map.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			log.error(Constants.ERROR_LOG, e);
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, Constants.SPACE);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	@Override
	public Map<String, Object> listAllRespondentUserAssessment(MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException {
		Map<String, Object> response = new HashMap<>();
		log.info(LogUtil.startLog(CLASSNAME));

		try {

			Map<String, Object> userResponse = ExceptionUtil
					.throwExceptionsIfPresent(userUtil.tokenVerification(headers));

			String timeZone = null;
			if (userResponse.get(Constants.USER_TIME_ZONE) != null) {
				timeZone = String.valueOf(userResponse.get(Constants.USER_TIME_ZONE));
			} else {
				timeZone = "(GMT-12:00) International Date Line West";
			}

			log.info(TIMEZONE_IS + timeZone);

			String zone = timeZone.substring(timeZone.indexOf("(") + 1, timeZone.indexOf(")"));

			log.info("Zone is  " + zone);

			Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));

			Map<String, List<AssessmentDto>> assessments = fetchAssessments(userId, zone);
			List<AssessmentDto> activeAssessments = assessments.get("active");
			List<AssessmentDto> completedAssessments = assessments.get("completed");

			response.put("activeAssessmentsDtoList", activeAssessments);
			response.put("completedAssessmentDtoList", completedAssessments);
			response.put(Constants.ERROR, null);
			response.put(Constants.SUCCESS, new SuccessResponse("Assessments fetched successfully"));

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			log.error(Constants.ERROR_LOG, e);
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, Constants.SPACE);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	private Map<String, List<AssessmentDto>> fetchAssessments(Long userId, String zone)
			throws ParseException, IOException, TechnicalException, BussinessException, ContractException {
		log.debug("Fetching all assessments for userId: {}", userId);
		List<AssessmentDto> activeAssessments = new ArrayList<>();
		List<AssessmentDto> completedAssessments = new ArrayList<>();

		List<AssessmentUserMapping> assessmentUserMappings = assessmentUserMappingRepository
				.findByUserIdAndActive(userId, Boolean.TRUE);

		for (AssessmentUserMapping userMapping : assessmentUserMappings) {
			if (userMapping.getAssessmentResponseStatus() == null) {
				continue;
			}

			Assessment assessment = assessmentRepository.findByIdAndActive(userMapping.getAssessmentId(), Boolean.TRUE);
			if (assessment == null) {
				continue;
			}

			AssessmentDto assessmentDto = new AssessmentDto();
			BeanUtils.copyProperties(assessment, assessmentDto);
			int totalQuestions = 0;
			int totalselectedQuestions = 0;
			double overallAvg = 0.0;
			List<Long> sectionIds = sectionRepository.findIdsByTemplateId(assessment.getTemplateId());
			AssessmentSubmittedResponseUser totalcount = assessmentSubmittedUserRepository
					.findByAssessmentIdAndUserId(assessment.getId(), userId);

			totalQuestions = (totalcount != null && totalcount.getTotalQuestionInOneAssessment() != null)
					? totalcount.getTotalQuestionInOneAssessment().intValue()
					: 0;

			if (CollectionUtils.isNotEmpty(sectionIds)) {
				boolean isTotalCountNull = (totalcount == null);

				for (Long secId : sectionIds) {
					if (isTotalCountNull) {
						totalQuestions += elementsRepository
								.countByTemplateIdAndSectionIdAndActiveTrue(assessment.getTemplateId(), secId);
					}

					Integer responseCount = sectionWiseStatusMappingRepository.findResponseCountInOneSection(secId,
							assessment.getId(), userId);
					totalselectedQuestions += (responseCount != null) ? responseCount : 0;

					SectionWiseRating avg = sectionWiseRatingRepository.findBySectionIdAndAssessmentIdAndUserId(secId,
							assessment.getId(), userId);
					overallAvg += (avg != null && avg.getSectionTotal() != null) ? avg.getSectionTotal() : 0.0;
				}
			}

			assessmentDto.setTotalQuestionInOneAssessment(totalQuestions);
			assessmentDto.setTotalResponseCountInOneAssessment(totalselectedQuestions);
			assessmentDto.setAverageScoreInOneAssessment(totalQuestions > 0 ? (overallAvg / totalQuestions) : 0.0);
			if (totalQuestions > 0) {
				double percentage = ((double) totalselectedQuestions / totalQuestions) * 100;
				BigDecimal formattedPercentage = BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP);
				assessmentDto.setUserResponsePercent(formattedPercentage.doubleValue());
			}

			addAssessmentInsights(assessmentDto, assessment);
			assessmentDto
					.setCategories(assessmentResponseServiceImpl.getCategoryMasterDtoList(assessment.getTemplateId()));
			dateAndNameSet(zone, assessment, assessmentDto);
			assessmentDto.setAssessmentResponseStatus(userMapping.getAssessmentResponseStatus().toString());
			if (userMapping.getAssessmentResponseStatus().equals(AssessmentResponseStatus.COMPLETED)) {
				completedAssessments.add(assessmentDto);
			} else {
				activeAssessments.add(assessmentDto);
			}
		}
		log.debug("Fetched {} active and {} completed assessments for userId: {}", activeAssessments.size(),
				completedAssessments.size(), userId);
		Map<String, List<AssessmentDto>> assessmentMap = new HashMap<>();
		assessmentMap.put("active", activeAssessments);
		assessmentMap.put("completed", completedAssessments);
		return assessmentMap;
	}

	private void dateAndNameSet(String zone, Assessment assessment, AssessmentDto assessmentDto)
			throws TechnicalException {
		if (ObjectUtils.isPositiveNonZero(assessment.getReviewer())) {
			assessmentDto.setReviewerName(userUtil.getUserFullName(assessment.getReviewer()));
		}
		if (ObjectUtils.isPositiveNonZero(assessment.getScheduledBy())) {
			assessmentDto.setScheduledByName(userUtil.getUserFullName(assessment.getScheduledBy()));
			assessmentDto
					.setScheduledOn(DateUtil.convertToTimeZoneById(assessment.getScheduledOn(), DATE_FORMAT, zone));
		}
		if (ObjectUtils.isPositiveNonZero(assessment.getOwner())) {
			assessmentDto.setOwnerName(userUtil.getUserFullName(assessment.getOwner()));
		}

		if (ObjectUtils.isPositiveNonZero(assessment.getCreatedBy())) {
			assessmentDto.setCreatedBy(userUtil.getUserFullName(assessment.getCreatedBy()));
			assessmentDto.setCreatedOn(DateUtil.convertToTimeZoneById(assessment.getCreatedOn(), DATE_FORMAT, zone));
		}
		assessmentDto.setExpiredOn(formatDate(assessment.getExpiredOn()));
	}

	public Map<String, Object> scheduleAssessment(ScheduleAssessmentDTO dto, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		Map<String, Object> response = new HashMap<>();
		Map<String, Object> userResponse = verifyToken(headers);
		try {
			Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
			Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

			Assessment assessment = assessmentRepository.findByIdAndCompanyId(dto.getAssessmentId(), companyId);
			if (assessment == null) {
				throw new BussinessException(HttpStatus.NOT_FOUND, NO_SUCH_ASSESSMENT_EXIST);
			}
			validateAssessmentStatusForSave(assessment, userId);
			assessment.setExpiredOn(dto.getExpiredOn());
			assessment.setScheduledBy(userId);
			assessment.setScheduledOn(Boolean.TRUE.equals(dto.getSentOnApproval()) ? new Date() : dto.getScheduledOn());
			assessment.setProgressStatus(AssessmentProgress.SCHEDULE);
			assessment.setSentOnApproval(dto.getSentOnApproval());
			if (assessment.getAssessmentDisplayId() == null) {
				setAssessmentDisplayId(companyId, assessment.getId(), assessment, headers);
			}

			if (!assessment.getOwner().equals(assessment.getReviewer())) {
				assessment.setStatus(AssessmentStatus.FOR_REVIEW);
			} else {
				assessment.setStatus(Boolean.TRUE.equals(dto.getSentOnApproval()) ? AssessmentStatus.ACTIVE
						: AssessmentStatus.SCHEDULED);
			}

			if (!userId.equals(assessment.getOwner()) && userId.equals(assessment.getReviewer())) {
				assessment.setStatus(Boolean.TRUE.equals(dto.getSentOnApproval()) ? AssessmentStatus.ACTIVE
						: AssessmentStatus.SCHEDULED);
				assessment.setReviewedOn(new Date());
			}

			assessment = assessmentRepository.save(assessment);

			if (AssessmentStatus.FOR_REVIEW.equals(assessment.getStatus())) {
				sendNotificationEmailUser(assessment, headers);
			}

			if (AssessmentStatus.ACTIVE.equals(assessment.getStatus())) {
				sendNotificationEmailToRespondentUser(assessment);
				assessment.setPublishedOn(new Date());
				assessment = assessmentRepository.save(assessment);
			}

			if (AssessmentStatus.SCHEDULED.equals(assessment.getStatus())) {
				assessmentSchedulerService.scheduleAssessment(assessment);
			}

			response.put(ASSESSMENT_ID, assessment.getId());
			response.put(Constants.SUCCESS, new SuccessResponse("Assessment Scheduled Successfully!"));
			response.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, Constants.SPACE);
		}
		return response;
	}

	public void sendNotificationEmailToRespondentUser(Assessment assessment)
			throws ContractException, TechnicalException, BussinessException {
		try {

			List<AssessmentUserMapping> assessmentUserMapping = assessmentUserMappingRepository
					.findByAssessmentIds(assessment.getId());

			if (CollectionUtils.isNotEmpty(assessmentUserMapping)) {
				assessmentUserMapping.forEach(u -> {
					u.setEmailReminder(AssessmentRemainder.FIRST);
					u.setLastReminderSent(new Date());
				});
				assessmentUserMappingRepository.saveAll(assessmentUserMapping);
			}

			List<Long> ids = assessmentUserMapping.stream().map(AssessmentUserMapping::getUserId).toList();

			if (CollectionUtils.isEmpty(ids)) {
				log.info("No user IDs found for assessment ID: {}", assessment.getId());
				return;
			}

			Date expiredOn = assessment.getExpiredOn();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
			String endDate = dateFormat.format(expiredOn);

			String userName = userUtil.getUserFullName(assessment.getCreatedBy());
			if (StringUtils.isBlank(userName)) {
				log.warn("User name is null or empty for createdBy: {}", assessment.getCreatedBy());
				return;
			}

			List<ExternalProjectionDTO> projectionDTOs = respondentDetailsRepository.findByUserIds(ids);
			if (CollectionUtils.isEmpty(projectionDTOs)) {
				log.info("No respondent details found for given user IDs.");
				return;
			}

			for (ExternalProjectionDTO dto : projectionDTOs) {
				if (dto == null || StringUtils.isBlank(dto.getEmailAddress())
						|| StringUtils.isBlank(dto.getFullName())) {
					log.warn("Skipping email notification for user with incomplete details: {}", dto);
					continue;
				}

				sendWorkFlowEmailForUser("EBITC010", assessment.getName(), dto.getEmailAddress(), userName, endDate,
						dto.getFullName());
				log.info("Triggered email sending for Respondent User: {}", dto.getFullName());
			}
		} catch (Exception e) {
			log.error("Error in sending notifications: {}", e.getMessage(), e);
		}
	}

	private Map<String, Object> verifyToken(MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		return ExceptionUtil.throwExceptionsIfPresent(userUtil.tokenVerification(headers));
	}

	@Override
	public Map<String, Object> playPauseAssessment(PlayPauseAssessmentDTO assessmentDto,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> response = new HashMap<>();
		Map<String, Object> userResponse = verifyToken(headers);

		try {
			Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

			Assessment assessment = assessmentRepository.findByIdAndCompanyId(assessmentDto.getAssessmentId(),
					companyId);

			if (assessment == null) {

				throw new BussinessException(HttpStatus.NOT_FOUND, NO_SUCH_ASSESSMENT_EXIST);

			}

			if (Boolean.TRUE.equals(assessmentDto.getIsAssessmentPlay())) {
				assessment.setIsAssessmentPlay(true);
				response.put(Constants.SUCCESS, new SuccessResponse("Assessment started successfully!"));
			} else {
				assessment.setIsAssessmentPlay(false);
				response.put(Constants.SUCCESS, new SuccessResponse("Assessment paused successfully!"));
			}

			assessmentRepository.save(assessment);

			response.put(Constants.ERROR, null);

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, Constants.SPACE);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	public String getSystemValueSerialId(String name, String value)
			throws ContractException, BussinessException, TechnicalException {
		if (name == null || name.isEmpty() || value == null || value.isEmpty()) {
			return null;
		}
		Map<String, Object> response = svmUtil.findByNameAndValue(name, value);
		if (response == null || !response.containsKey(Constants.SERIAL_ID)) {
			return null;
		}
		String svmValue = String.valueOf(response.get("value")).toUpperCase();
		String inputValue = value.toUpperCase();
		if (svmValue.equals(inputValue)) {
			return String.valueOf(response.get(Constants.SERIAL_ID));
		}
		return null;
	}

	private String getValidatedCellValue(Cell cell) {
		DataFormatter formatter = new DataFormatter();
		String cellValue = formatter.formatCellValue(cell).trim();
		return cellValue.isEmpty() ? null : CSVValidatorUtil.checkRegex(cellValue);

	}

	@Override
	@CustomTransactional
	public Map<String, Object> bulkUploadExcelRespondentUser(Long assessmentId, MultipartFile file,
			MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		String date = DateFormatUtils.format(new Date(), DateUtil.DATE_FOTMAT1);
		Integer successCount = 0;
		Integer errorCount = 0;
		String str = Constants.EMPTYSTRING;
		DataFormatter formatter = new DataFormatter();

		log.info("Checking File size do not Exceed 5 MB Limit");
		FileSize.validateIfSizeGreaterThan(file, FileSize.FIVE_MB);

		InputStream is = file.getInputStream();
		log.info("File ContentType : {}", file.getContentType());
		log.info("File OriginalFilename : {}", file.getOriginalFilename());

		FileValidatorUtil.validateExcelOrCSV(file);

		try (Workbook workbook = new XSSFWorkbook(is)) {

			Sheet sheet = workbook.getSheet("RESPONDENT_USER");

			Map<String, Object> userResponse = verifyToken(headers);
			Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
			Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

			Assessment assessment = assessmentRepository.findByIdAndCompanyId(assessmentId, companyId);
			if (assessment == null) {
				throw new BussinessException(HttpStatus.NOT_FOUND, ASSESSMENT_NOT_AVAILABLE);
			}

			validateAssessmentStatusForSave(assessment, userId);

			if (sheet != null) {

				XSSFCellStyle errorStyle = (XSSFCellStyle) workbook.createCellStyle();
				XSSFCellStyle successStyle = (XSSFCellStyle) workbook.createCellStyle();
				XSSFFont redFont = (XSSFFont) workbook.createFont();
				byte[] rgb = new byte[] { (byte) 255, (byte) 51, (byte) 0 }; // RGB for red
				XSSFColor redColor = new XSSFColor(rgb, new DefaultIndexedColorMap());
				redFont.setColor(redColor);
				errorStyle.setFont(redFont);

				XSSFFont greenFont = (XSSFFont) workbook.createFont();
				byte[] rgbGreen = new byte[] { (byte) 0, (byte) 255, (byte) 0 }; // RGB for green
				XSSFColor greenColor = new XSSFColor(rgbGreen, new DefaultIndexedColorMap());
				greenFont.setColor(greenColor);
				successStyle.setFont(greenFont);

				for (Row row : sheet) {

					if (row == sheet.getRow(0) || FileValidatorUtil.isRowEmpty(row)) {
						continue;
					}

					String title = getValidatedCellValue(row.getCell(0));
					String firstName = getValidatedCellValue(row.getCell(1));
					String middleName = getValidatedCellValue(row.getCell(2));
					String lastName = getValidatedCellValue(row.getCell(3));
					String gender = getValidatedCellValue(row.getCell(4));
					String emailAddress = getValidatedCellValue(row.getCell(5));
					String mobileNo = getValidatedCellValue(row.getCell(6));
					String dateOfBirth = getValidatedCellValue(row.getCell(7));

					CSVValidatorUtil.checkRegex(formatter.formatCellValue(row.getCell(47)).trim());
					CSVValidatorUtil.checkRegex(formatter.formatCellValue(row.getCell(48)).trim());

					row.createCell(47);
					row.createCell(48);

					// Validate row
					List<String> errors = ExcelValidator.validateRow(firstName, lastName, gender, emailAddress,
							mobileNo, dateOfBirth);

					if (CollectionUtils.isNotEmpty(errors)) {
						int rowNum = row.getRowNum();
						row.getCell(47).setCellValue(Constants.ERROR);
						row.getCell(48).setCellValue("Row " + (rowNum + 1) + ": " + String.join(", ", errors));
						row.getCell(47).setCellStyle(errorStyle);
						row.getCell(48).setCellStyle(errorStyle);
						errorCount++;

						continue;
					}

					String weight = getValidatedCellValue(row.getCell(8));
					String heightFt = getValidatedCellValue(row.getCell(9));
					String heightIn = getValidatedCellValue(row.getCell(10));
					String bmiValue = getValidatedCellValue(row.getCell(11));
					String bloodGroup = getValidatedCellValue(row.getCell(12));
					String maritalStatus = getValidatedCellValue(row.getCell(13));
					String spouseName = getValidatedCellValue(row.getCell(14));
					String spouseDateOfBirth = getValidatedCellValue(row.getCell(15));
					String spouseGender = getValidatedCellValue(row.getCell(16));
					String spouseWork = getValidatedCellValue(row.getCell(17));
					String spouseBloodGroup = getValidatedCellValue(row.getCell(18));
					String childrenCount = getValidatedCellValue(row.getCell(19));
					String fatherAlive = getValidatedCellValue(row.getCell(20));
					String fatherName = getValidatedCellValue(row.getCell(21));
					String fatherBloodGroup = getValidatedCellValue(row.getCell(22));
					String motherAlive = getValidatedCellValue(row.getCell(23));
					String motherName = getValidatedCellValue(row.getCell(24));
					String motherBloodGroup = getValidatedCellValue(row.getCell(25));
					String fatherInLawAlive = getValidatedCellValue(row.getCell(26));
					String fatherInLawName = getValidatedCellValue(row.getCell(27));
					String fatherInLawBloodGroup = getValidatedCellValue(row.getCell(28));
					String motherInLawAlive = getValidatedCellValue(row.getCell(29));
					String motherInLawName = getValidatedCellValue(row.getCell(30));
					String motherInLawBloodGroup = getValidatedCellValue(row.getCell(31));
					String corporateHealthPolicyFlag = getValidatedCellValue(row.getCell(32));
					String corporateHealthPolicyCoverage = getValidatedCellValue(row.getCell(33));
					String mealPreference = getValidatedCellValue(row.getCell(34));
					String child1Plan = getValidatedCellValue(row.getCell(35));
					String child1Name = getValidatedCellValue(row.getCell(36));
					String child1Gender = getValidatedCellValue(row.getCell(37));
					String child1DateOfBirth = getValidatedCellValue(row.getCell(38));
					String child1BloodGroup = getValidatedCellValue(row.getCell(39));
					String child1Work = getValidatedCellValue(row.getCell(40));

					String child2Plan = getValidatedCellValue(row.getCell(41));
					String child2Name = getValidatedCellValue(row.getCell(42));
					String child2Gender = getValidatedCellValue(row.getCell(43));
					String child2DateOfBirth = getValidatedCellValue(row.getCell(44));
					String child2BloodGroup = getValidatedCellValue(row.getCell(45));
					String child2Work = getValidatedCellValue(row.getCell(46));

					String error = Constants.EMPTYSTRING;

					if (StringUtils.isEmpty(emailAddress)) {
						error = "emailAddress is empty";
					}

					UserMasterDTO respondentUser = new UserMasterDTO();
					respondentUser.setEmailAddress(emailAddress);
					respondentUser.setLoginId(emailAddress);
					respondentUser.setCompanyId(companyId);
					respondentUser.setMobileNo(mobileNo);
					respondentUser.setUserFirstName(firstName);
					respondentUser.setUserLastName(lastName);
					SimpleDateFormat dateFormat = new SimpleDateFormat(YYYY_MM_DD);
					dateFormat.setLenient(false);
					Date userDateOfBirth = dateFormat.parse(dateOfBirth);
					respondentUser.setUserDateOfBirth(userDateOfBirth);
					respondentUser.setUserId(0L);
					respondentUser.setTimeZone("55");
					respondentUser.setGender(gender != null ? getSystemValueSerialId(GENDER, gender) : null);
					respondentUser.setMaritalStatus(
							maritalStatus != null ? getSystemValueSerialId(MARITAL_STATUS, maritalStatus) : null);

					headers.remove("content-length");
					Map<String, Object> uMapResp = userUtil.addRespondentUser(respondentUser, headers);

					if (uMapResp.containsKey(Constants.ERROR) && uMapResp.get(Constants.ERROR) instanceof Map) {
						Map<String, String> errorMessages = (Map<String, String>) uMapResp.get(Constants.ERROR);
						if (!errorMessages.isEmpty()) {
							String present = "User with email address '" + emailAddress
									+ "' already exists in another company";
							Object oldUserIdObj = errorMessages.get("oldUserId");

							if (errorMessages.containsKey("companyId")) {
								int rowNum = row.getRowNum();

								row.getCell(47).setCellValue(Constants.ERROR);
								row.getCell(48).setCellValue("Row " + (rowNum + 1) + ": " + present);
								row.getCell(47).setCellStyle(errorStyle);
								row.getCell(48).setCellStyle(errorStyle);

								errorCount++;
								log.info("Row " + (rowNum + 1) + ": " + present);
								continue;
							}

							if (oldUserIdObj != null) {
								Long oldUserId = Long.valueOf(oldUserIdObj.toString());
								AssessmentUserMapping userMapping = assessmentUserMappingRepository
										.findByAssessmentIdAndUserId(assessmentId, oldUserId);
								if (userMapping == null) {
									userMapping = new AssessmentUserMapping();
									userMapping.setActive(Boolean.TRUE);
									userMapping.setAssessmentId(assessmentId);
									userMapping.setUserId(oldUserId);
									userMapping.setCreatedBy(userId);
									userMapping.setCreatedOn(new Date());
									userMapping.setAssessmentResponseStatus(AssessmentResponseStatus.PENDING);
									assessmentUserMappingRepository.save(userMapping);
								}
								row.getCell(47).setCellValue(Constants.SUCCESS);
								row.getCell(48).setCellValue(present);
								row.getCell(47).setCellStyle(successStyle);
								row.getCell(48).setCellStyle(successStyle);
								successCount++;
								continue;
							}
						}
					}

					if (StringUtils.isEmpty(error)) {
						Long newUserId = 0L;
						if (uMapResp.containsKey(Constants.SUCCESS)) {
							newUserId = Long.parseLong(String.valueOf(uMapResp.get(Constants.USER_ID)));
						}

						AssessmentUserMapping userMapping = new AssessmentUserMapping();
						userMapping.setActive(Boolean.TRUE);
						userMapping.setAssessmentId(assessmentId);
						userMapping.setUserId(newUserId);
						userMapping.setCreatedBy(userId);
						userMapping.setCreatedOn(new Date());
						userMapping.setAssessmentResponseStatus(AssessmentResponseStatus.PENDING);
						assessmentUserMappingRepository.save(userMapping);

						RespondentDetails respondentDetails = new RespondentDetails();
						respondentDetails.setUserId(newUserId);
						respondentDetails.setTitle(title != null ? getSystemValueSerialId(TITLE, title) : null);
						respondentDetails.setFirstName(firstName != null ? firstName : null);
						respondentDetails.setMiddleName(middleName != null ? middleName : null);
						respondentDetails.setLastName(lastName != null ? lastName : null);

						respondentDetails.setGender(gender != null ? getSystemValueSerialId(GENDER, gender) : null);
						respondentDetails.setEmailAddress(emailAddress != null ? emailAddress : null);
						respondentDetails.setDateOfBirth(
								dateOfBirth != null ? new SimpleDateFormat(YYYY_MM_DD).parse(dateOfBirth) : null);

						respondentDetails.setWeight(weight != null ? Integer.parseInt(weight) : null);
						respondentDetails.setHeightFt(heightFt != null ? Integer.parseInt(heightFt) : null);
						respondentDetails.setHeightIn(heightIn != null ? Integer.parseInt(heightIn) : null);
						respondentDetails.setBmiValue(bmiValue != null ? Integer.parseInt(bmiValue) : null);
						respondentDetails.setBloodGroup(
								bloodGroup != null ? getSystemValueSerialId(BLOOD_GROUP, bloodGroup) : null);
						respondentDetails.setMaritalStatus(
								maritalStatus != null ? getSystemValueSerialId(MARITAL_STATUS, maritalStatus) : null);

						if ((maritalStatus != null && maritalStatus.equals("Married"))
								|| "142".equals(respondentDetails.getMaritalStatus())) {
							respondentDetails.setSpouseName(spouseName != null ? spouseName : null);
							respondentDetails.setSpouseGender(
									spouseGender != null ? getSystemValueSerialId(GENDER, spouseGender) : null);
							respondentDetails.setSpouseWork(
									spouseWork != null ? getSystemValueSerialId(WORK_STATUS, spouseWork) : null);
							respondentDetails.setSpouseBloodGroup(
									spouseBloodGroup != null ? getSystemValueSerialId(BLOOD_GROUP, spouseBloodGroup)
											: null);
							respondentDetails.setSpouseDateOfBirth(spouseDateOfBirth != null
									? new SimpleDateFormat(YYYY_MM_DD).parse(spouseDateOfBirth)
									: null);

							respondentDetails.setDateOfBirth(
									dateOfBirth != null ? new SimpleDateFormat(YYYY_MM_DD).parse(dateOfBirth) : null);
						}
						respondentDetails
								.setChildrenCount(childrenCount != null ? Integer.parseInt(childrenCount) : null);

						respondentDetails.setFatherAlive("Yes".equalsIgnoreCase(fatherAlive) ? Boolean.TRUE
								: "No".equalsIgnoreCase(fatherAlive) ? Boolean.FALSE : null);
						if (Boolean.TRUE.equals(respondentDetails.getFatherAlive())) {

							respondentDetails.setFatherName(fatherName != null ? fatherName : null);
							respondentDetails.setFatherBloodGroup(
									fatherBloodGroup != null ? getSystemValueSerialId(BLOOD_GROUP, fatherBloodGroup)
											: null);
						}

						respondentDetails.setMotherAlive("Yes".equalsIgnoreCase(motherAlive) ? Boolean.TRUE
								: "No".equalsIgnoreCase(motherAlive) ? Boolean.FALSE : null);

						if (Boolean.TRUE.equals(respondentDetails.getMotherAlive())) {
							respondentDetails.setMotherName(motherName != null ? motherName : null);
							respondentDetails.setMotherBloodGroup(
									motherBloodGroup != null ? getSystemValueSerialId(BLOOD_GROUP, motherBloodGroup)
											: null);
						}

						respondentDetails.setFatherInLawAlive("Yes".equalsIgnoreCase(fatherInLawAlive) ? Boolean.TRUE
								: "No".equalsIgnoreCase(fatherInLawAlive) ? Boolean.FALSE : null);
						if (Boolean.TRUE.equals(respondentDetails.getFatherInLawAlive())) {
							respondentDetails.setFatherInLawName(fatherInLawName != null ? fatherInLawName : null);
							respondentDetails.setFatherInLawBloodGroup(fatherInLawBloodGroup != null
									? getSystemValueSerialId(BLOOD_GROUP, fatherInLawBloodGroup)
									: null);
						}

						respondentDetails.setMotherInLawAlive("Yes".equalsIgnoreCase(motherInLawAlive) ? Boolean.TRUE
								: "No".equalsIgnoreCase(motherInLawAlive) ? Boolean.FALSE : null);

						if (Boolean.TRUE.equals(respondentDetails.getMotherInLawAlive())) {
							respondentDetails.setMotherInLawName(motherInLawName != null ? motherInLawName : null);
							respondentDetails.setMotherInLawBloodGroup(motherInLawBloodGroup != null
									? getSystemValueSerialId(BLOOD_GROUP, motherInLawBloodGroup)
									: null);
						}

						respondentDetails.setCorporateHealthPolicyFlag(
								"Yes".equalsIgnoreCase(corporateHealthPolicyFlag) ? Boolean.TRUE
										: "No".equalsIgnoreCase(corporateHealthPolicyFlag) ? Boolean.FALSE : null);
						respondentDetails.setCorporateHealthPolicyName(
								corporateHealthPolicyCoverage != null ? corporateHealthPolicyCoverage : null);
						respondentDetails.setMealPreference(
								mealPreference != null ? getSystemValueSerialId(MEAL_PREFERENCE, mealPreference)
										: null);

						respondentDetails.setChild1Plan(
								child1Plan != null ? getSystemValueSerialId(CHILD_PLANNING, child1Plan) : null);

						if ((child1Plan != null && child1Plan.equals("Is Born"))
								|| ("159".equals(respondentDetails.getChild1Plan()))) {
							respondentDetails.setChild1Name(child1Name != null ? child1Name : null);
							respondentDetails.setChild1DateOfBirth(
									dateOfBirth != null ? new SimpleDateFormat(YYYY_MM_DD).parse(child1DateOfBirth)
											: null);
							respondentDetails.setChild1Gender(
									child1Gender != null ? getSystemValueSerialId(GENDER, child1Gender) : null);
							respondentDetails.setChild1BloodGroup(
									child1BloodGroup != null ? getSystemValueSerialId(BLOOD_GROUP, child1BloodGroup)
											: null);
							respondentDetails.setChild1Work(
									child1Work != null ? getSystemValueSerialId(WORK_STATUS, child1Work) : null);
						}

						respondentDetails.setChild2Plan(
								child2Plan != null ? getSystemValueSerialId(CHILD_PLANNING, child2Plan) : null);

						if ((child2Plan != null && child2Plan.equals("Is Born"))
								|| ("159".equals(respondentDetails.getChild2Plan()))) {

							respondentDetails.setChild2Name(child2Name != null ? child2Name : null);
							respondentDetails.setChild2DateOfBirth(
									dateOfBirth != null ? new SimpleDateFormat(YYYY_MM_DD).parse(child2DateOfBirth)
											: null);
							respondentDetails.setChild2Gender(
									child2Gender != null ? getSystemValueSerialId(GENDER, child2Gender) : null);
							respondentDetails.setChild2BloodGroup(
									child2BloodGroup != null ? getSystemValueSerialId(BLOOD_GROUP, child2BloodGroup)
											: null);
							respondentDetails.setChild2Work(
									child2Work != null ? getSystemValueSerialId(WORK_STATUS, child2Work) : null);
						}
						respondentDetails.setCreatedBy(userId);
						respondentDetails.setCreatedOn(new Date());

						respondentDetailsRepository.save(respondentDetails);

						row.getCell(47).setCellValue(Constants.SUCCESS);
						row.getCell(47).setCellStyle(successStyle);
						successCount++;
					} else {
						row.getCell(47).setCellValue(Constants.ERROR);
						row.getCell(48).setCellValue(error);
						row.getCell(47).setCellStyle(errorStyle);
						row.getCell(48).setCellStyle(errorStyle);
						errorCount++;
					}

				}
				log.info("Starting File Generation.............................");
				str = "Respondent User Import" + date;
				str = str.replaceAll("\\s+", Constants.EMPTYSTRING);
				str = str.replace(":", Constants.EMPTYSTRING);

				File errorFile = new File(excelPath + str + Constants.XLSX);
				Path errorFilePath = errorFile.toPath();
				if (!errorFile.exists()) {
					if (!errorFile.createNewFile()) {
						log.warn("Failed to create new file: " + errorFile.getAbsolutePath());
					}
				} else {
					if (errorFile.exists()) {
						Files.delete(errorFilePath);
					}
				}
				handleFileOperations(errorFile, fileManager, excelPath, str, workbook);
			} else {
				log.info("RESPONDENT_USER Sheet not found ---------------->> ");
				throw new BussinessException(HttpStatus.BAD_REQUEST, "RESPONDENT_USER Sheet not found");
			}

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		map.put(Constants.STATUS_FILE, staticDocExcelPath + str + Constants.XLSX);
		map.put(Constants.TOTAL_COUNT, successCount + errorCount);
		map.put(Constants.SUCCESS_COUNT, successCount);
		map.put(Constants.ERROR_COUNT, errorCount);
		map.put(Constants.SUCCESS, new SuccessResponse("file uploaded"));
		map.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	private void handleFileOperations(File errorFile, FileManager fileManager, String docFolder, String str,
			Workbook workbook) {
		log.info(" Inside handleFileOperations .............................");
		try (FileOutputStream outputStream = new FileOutputStream(errorFile)) {
			workbook.write(outputStream);
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
		}

		try {
			fileManager.saveExcel(docFolder, str + Constants.XLSX, errorFile, Boolean.FALSE);
			log.info(" FileGenerated .............................");
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
		} finally {
			if (errorFile.exists()) {
				try {
					Files.delete(errorFile.toPath());
				} catch (IOException e) {
					log.warn("Failed to delete the error file: " + errorFile.getAbsolutePath(), e);
				}
			}
		}
	}

	@Override
	public Map<String, Object> getRespondentUserDetails(Long userId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		verifyToken(headers);
		Map<String, Object> map = new HashMap<>();
		try {

			RespondentDetails details = respondentDetailsRepository.findByUserId(userId);
			if (details == null) {
				throw new BussinessException(HttpStatus.NOT_FOUND, "Respondent details not found");
			}
			RespondentDetailDTO dto = buildRespondentDetailsDTO(details);

			if (details != null && ObjectUtils.isPositiveNonZero(details.getProfileImageId())) {
				DocumentGetDto documentGetDto = getProfileImage(details.getProfileImageId());
				dto.setProfileImage(documentGetDto);
			}

			map.put("respondentUserDTO", dto);
			map.put(Constants.SUCCESS, new SuccessResponse(Constants.SUCCESS));
			map.put(Constants.ERROR, null);

		} catch (Exception e) {
			log.error("Technical exception occurred: {}", LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return map;
	}

	private RespondentDetailDTO buildRespondentDetailsDTO(RespondentDetails details) {
		RespondentDetailDTO dto = new RespondentDetailDTO();
		BeanUtils.copyProperties(details, dto);

		List<RespondentCoverageInfoMapping> corporateCoverageMappings = coverageInfoMappingRepository
				.findByUserIdAndPolicyType(details.getUserId(), PolicyType.CORPORATE);

		if (corporateCoverageMappings != null && !corporateCoverageMappings.isEmpty()) {
			List<CoverageDTO> corporateCoverageDTOList = corporateCoverageMappings.stream().map(mapping -> {
				CoverageDTO coverageDTO = new CoverageDTO();
				coverageDTO.setId(mapping.getCoverageId());
				coverageDTO.setFlag(mapping.getCoverageflag());
				return coverageDTO;
			}).collect(Collectors.toList());
			dto.setCorporateCoverageDTO(corporateCoverageDTOList);
		}

		List<RespondentCoverageInfoMapping> personalCoverageMappings = coverageInfoMappingRepository
				.findByUserIdAndPolicyType(details.getUserId(), PolicyType.PERSONAL);

		if (personalCoverageMappings != null && !personalCoverageMappings.isEmpty()) {
			List<CoverageDTO> personalCoverageDTOList = personalCoverageMappings.stream().map(mapping -> {
				CoverageDTO coverageDTO = new CoverageDTO();
				coverageDTO.setId(mapping.getCoverageId());
				coverageDTO.setFlag(mapping.getCoverageflag());
				return coverageDTO;
			}).collect(Collectors.toList());
			dto.setPersonalCoverageDTO(personalCoverageDTOList);
		}

		dto.setUserId(details.getUserId());
		dto.setProfileImageId(details.getProfileImageId());

		log.info(LogUtil.exitLog(CLASSNAME));
		return dto;
	}

	private DocumentGetDto getProfileImage(Long profileImageId)
			throws TechnicalException, BussinessException, ContractException {
		Map<String, Object> oauthReturnMap = ExceptionUtil
				.throwExceptionsIfPresent(documentServiceFeignProxy.getDocument(profileImageId));

		if (MapUtils.isNotEmpty(oauthReturnMap) && oauthReturnMap.containsKey("documentDto")) {
			String json = new Gson().toJson(oauthReturnMap.get("documentDto"));
			try {
				return new ObjectMapper().readValue(json, DocumentGetDto.class);
			} catch (Exception e) {
				throw new TechnicalException("Error processing profile image", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return null;
	}

	@Override
	public Map<String, Object> updateRespondentProfile(RespondentDetailsDTO dto, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {

		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> verifyToken = verifyToken(headers);
		Long loggedInUserId = Long.parseLong(String.valueOf(verifyToken.get(Constants.USER_MASTER_ID)));

		try {
			Long userId = dto.getUserId();

			RespondentDetails details = respondentDetailsRepository.findByUserId(userId);

			if (details == null) {
				log.error("Respondent details not found");
				throw new BussinessException(HttpStatus.NOT_FOUND, "Respondent details not found");
			}

			if (dto != null) {
				if (dto.getAboutMe() != null) {
					PropertyUtils.copyNonNullProperties(dto.getAboutMe(), details);
				}
				if (dto.getFamilyDTO() != null) {
					PropertyUtils.copyNonNullProperties(dto.getFamilyDTO(), details);
				}
				if (dto.getPolicyDTO() != null) {
					PropertyUtils.copyNonNullProperties(dto.getPolicyDTO(), details);
					savePolicyCoverage(dto.getPolicyDTO(), userId);
				}
			}

			if (dto.getProfileImage() != null && dto.getProfileImage().getId() == null
					&& StringUtils.isNotEmpty(dto.getProfileImage().getBase64())
					&& StringUtils.isNotEmpty(dto.getProfileImage().getDocumentName())) {
				uploadImageIfPresent(dto, details);
			}

			details.setModifiedBy(loggedInUserId);
			details.setModifiedOn(new Date());

			respondentDetailsRepository.save(details);

			updateUserMaster(details, headers);

			map.put(Constants.SUCCESS, new SuccessResponse("Respondent Details Updated Successfully!"));
			map.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error("Technical exception occurred: {}", LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	private void updateUserMaster(RespondentDetails details, MultiValueMap<String, String> headers)
			throws TechnicalException, ContractException, BussinessException {
		UserMasterDTO respondentUser = new UserMasterDTO();
		respondentUser.setEmailAddress(details.getEmailAddress());
		respondentUser.setLoginId(details.getEmailAddress());
		respondentUser.setUserFirstName(details.getFirstName());
		respondentUser.setUserLastName(details.getLastName());
		respondentUser.setUserDateOfBirth(details.getDateOfBirth());
		respondentUser.setUserId(details.getUserId());
		respondentUser.setGender(details.getGender());
		respondentUser.setMaritalStatus(details.getMaritalStatus());
		respondentUser.setProfileImageId(details.getProfileImageId());
		respondentUser.setTimeZone("55");
		headers.remove("content-length");
		Map<String, Object> uMapResp = userUtil.addRespondentUser(respondentUser, headers);
		if (uMapResp.containsKey(Constants.SUCCESS)) {
			Long newUserId = Long.parseLong(String.valueOf(uMapResp.get(Constants.USER_ID)));
			if (newUserId != null) {
				log.info("UserMaster Updated Successfully with UserId : " + newUserId);
			}
		}
	}

	private void uploadImageIfPresent(RespondentDetailsDTO profile, RespondentDetails details)
			throws BussinessException, ContractException, TechnicalException, IOException {
		int lastIndxDot = profile.getProfileImage().getDocumentName().lastIndexOf('.');
		String fileName = profile.getProfileImage().getDocumentName().substring(0, lastIndxDot);
		String ex = profile.getProfileImage().getDocumentName().substring(lastIndxDot + 1,
				profile.getProfileImage().getDocumentName().length());
		DocumentDto documentDto = new DocumentDto();
		documentDto.setFileName(fileName);
		documentDto.setExtension(ex);
		documentDto.setBase64(profile.getProfileImage().getBase64());
		documentDto.setSize("1");
		BeanUtils.copyProperties(profile.getProfileImage(), documentDto);
		Long docId = documentUtil.addDocument(documentDto);
		details.setProfileImageId(docId);
	}

	private void processCoverageList(List<CoverageDTO> coverageList, PolicyType policyType, Long userId) {
		if (coverageList != null && !coverageList.isEmpty()) {
			List<RespondentCoverageInfoMapping> mappings = new ArrayList<>();
			coverageInfoMappingRepository.deleteByUserIdAndPolicyType(userId, policyType);
			for (CoverageDTO coverage : coverageList) {
				Long id = coverage.getId();
				Boolean flag = coverage.getFlag();

				RespondentCoverageInfoMapping mapping = new RespondentCoverageInfoMapping();
				mapping.setCoverageId(id);
				mapping.setUserId(userId);
				mapping.setCoverageflag(flag);
				mapping.setActive(Boolean.TRUE);
				mapping.setPolicyType(policyType);
				mapping.setCreatedBy(userId);
				mapping.setCreatedOn(new Date());

				mappings.add(mapping);
			}

			coverageInfoMappingRepository.saveAll(mappings);
		}
	}

	public void savePolicyCoverage(PolicyDTO policyDTO, Long userId) {
		if (policyDTO.getCorporateHealthPolicyFlag() != null && policyDTO.getCorporateHealthPolicyFlag()) {
			processCoverageList(policyDTO.getCorporateCoverageDTO(), PolicyType.CORPORATE, userId);
		}

		if (policyDTO.getPersonalHealthPolicyFlag() != null && policyDTO.getPersonalHealthPolicyFlag()) {
			processCoverageList(policyDTO.getPersonalCoverageDTO(), PolicyType.PERSONAL, userId);
		}
	}

	@Override
	public Map<String, Object> findPaginatedRespondentsByAssessment(ListingDto listingDto, Long assessmentId,
			MultiValueMap<String, String> headers) throws BussinessException, TechnicalException, ContractException {

		Map<String, Object> map = new HashMap<>();
		log.info(LogUtil.startLog(CLASSNAME));
		verifyToken(headers);

		try {
			int length = listingDto.getLength();

			if (length == 0) {
				length = Constants.ONE_BILLION;
				listingDto.setLength(length);
			}

			listingDto.setColumnNames(columnsByListingType.get("assessmentIds"));
			listingDto.setJoinTableColumnNames(columnsByListingType.get("UserListing"));

			List<RespondentUserDTO> dtoList = Paginate.columnPaginate(em, listingDto, AssessmentUserMapping.class,
					RespondentUserDTO.class, USER_ID, RespondentDetails.class, USER_ID, assessmentId);

			if (CollectionUtils.isEmpty(dtoList)) {
				map.put(Constants.COUNT, 0);
				map.put(Constants.COLUMN, null);
				map.put(Constants.DTO_LIST, dtoList);
				map.put(Constants.SUCCESS, new SuccessResponse());
				map.put(Constants.ERROR, null);
				return map;
			}

			setAssessmentResponseStatus(dtoList);

			map.put(Constants.COUNT, Paginate.fetchRowsCount(em, listingDto, AssessmentUserMapping.class, USER_ID,
					RespondentDetails.class, USER_ID, assessmentId));
			map.put(Constants.COLUMN, null);
			map.put(Constants.DTO_LIST, dtoList);
			map.put(Constants.SUCCESS, new SuccessResponse());
			map.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	private static Map<String, List<String>> columnsByListingType = new HashMap<>();
	static {
		InputStream inputStream = AssessmentServiceImpl.class.getClassLoader().getResourceAsStream(PATH);
		try {
			columnsByListingType = new ObjectMapper().readValue(inputStream, Map.class);
		} catch (IOException e) {
			log.error("Not able to instantaite columnsByListingType MAP !");
		}
	}

	private void setAssessmentResponseStatus(List<RespondentUserDTO> dtoList)
			throws ContractException, BussinessException, TechnicalException {
		for (RespondentUserDTO dto : dtoList) {

			if (dto.getCreatedBy() != null) {
				String userName = userUtil.getUserFullName(dto.getCreatedBy());
				dto.setCreatedByName(userName);
			}

			AssessmentUserMapping assessmentUserMapping = assessmentUserMappingRepository
					.findAssessmentUserMappingByUserIdAndAssessmentId(dto.getUserId(), dto.getAssessmentId());

			if (assessmentUserMapping != null) {
				AssessmentResponseStatus status = assessmentUserMapping.getAssessmentResponseStatus();
				String statusName = (status != null) ? status.name() : null;
				dto.setAssessmentResponseStatus(statusName);
			}
		}
	}

	@Override
	public Map<String, Object> getAssessmentInsightsDashboard(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		try {
			AssessmentCountsDTO countDto = calculateAssessmentCounts(assessmentId);

			map.put("assessmentCounts", countDto);
			map.put(Constants.SUCCESS, new SuccessResponse("Insights Dashboard Details Found!"));
			map.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error("Technical exception occurred in getAssessmentInsightsDashboard: {}", LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.startLog(CLASSNAME));
		return map;
	}

	private AssessmentCountsDTO calculateAssessmentCounts(Long assessmentId) {
		AssessmentCountsDTO countDto = new AssessmentCountsDTO();
		Long sentToCount = assessmentUserMappingRepository.countByAssessmentId(assessmentId);
		countDto.setAssessmentSentTo(sentToCount != null ? sentToCount : 0L);

		Long respondedCount = assessmentUserMappingRepository
				.countByAssessmentIdAndAssessmentResponseStatus(assessmentId, AssessmentResponseStatus.COMPLETED);

		countDto.setAssessmentResponded(respondedCount != null ? respondedCount : 0L);

		List<Long> userIds = assessmentUserMappingRepository.findUserIdsByAssessmentId(assessmentId,
				AssessmentResponseStatus.PENDING);

		long inProgress = 0L;
		long notStarted = 0L;

		for (Long userId : userIds) {
			if (!sectionWiseRatingRepository.existsByAssessmentIdAndUserId(assessmentId, userId)) {
				notStarted++;
			}
		}

		List<Long> inprogress = assessmentUserMappingRepository.findUserIdsByAssessmentId(assessmentId,
				AssessmentResponseStatus.INPROGRESS);

		for (Long userId : inprogress) {
			if (sectionWiseRatingRepository.existsByAssessmentIdAndUserId(assessmentId, userId)) {
				inProgress++;
			}
		}
		countDto.setAssessmentInProgress(inProgress);
		countDto.setAssessmentNotStarted(notStarted);
		countDto.calculateCompletionPercentage();
		return countDto;
	}

	@Override
	public Map<String, Object> getAssessmentInsightsDetails(Long assessmentId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> userResponse = verifyToken(headers);
		Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));
		AssessmentInsightsDetailsDTO dto = new AssessmentInsightsDetailsDTO();
		try {
			Assessment assessment = findAssessment(assessmentId, companyId);

			buildDTO(assessment, dto);

			map.put("assessmentDto", dto);
			map.put(Constants.SUCCESS, new SuccessResponse("Assessment Insights Details Found!"));
			map.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error("Technical exception occurred in getAssessmentInsightsDashboard: {}", LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.startLog(CLASSNAME));
		return map;
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

	private void buildDTO(Assessment assessment, AssessmentInsightsDetailsDTO dto)
			throws ContractException, TechnicalException, BussinessException {
		BeanUtils.copyProperties(assessment, dto);
		List<Section> sections = sectionRepository.findByTemplateIdAndActive(assessment.getTemplateId(), Boolean.TRUE);
		if (sections == null || sections.isEmpty()) {
			return;
		}

		List<SectionDto> sectionListDto = sections.stream().map(section -> {
			SectionDto sectionDto = new SectionDto();
			BeanUtils.copyProperties(section, sectionDto);
			return sectionDto;
		}).collect(Collectors.toList());
		dto.setSections(sectionListDto);
	}

	@Override
	public Map<String, Object> getTemplateSectionsByAssessmentId(Long assessmentId,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException {

		Map<String, Object> map = new HashMap<>();
		log.info(LogUtil.startLog(CLASSNAME));

		try {

			Map<String, Object> userResponse = verifyToken(headers);
			Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

			Assessment assessment = findAssessment(assessmentId, companyId);

			List<Section> sections = sectionRepository.findByTemplateIdAndActive(assessment.getTemplateId(),
					Boolean.TRUE);

			map.put("sections", sections);

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.startLog(CLASSNAME));
		return map;

	}

}