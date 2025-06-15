package com.ehr.companymanagement.business.serviceimpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.ehr.companymanagement.business.dto.CompanyDetailsDTO;
import com.ehr.companymanagement.business.dto.CompanyDetailsListingDTO;
import com.ehr.companymanagement.business.dto.CompanyPaymentDTO;
import com.ehr.companymanagement.business.dto.SubscriptionPlansDto;
import com.ehr.companymanagement.business.service.CompanyDetailsService;
import com.ehr.companymanagement.business.validation.CompanyManagmentValidatorUtil;
import com.ehr.companymanagement.business.validation.ConstantsUtil;
import com.ehr.companymanagement.business.validation.EnglishConstants;
import com.ehr.companymanagement.integration.domain.CompanyDetails;
import com.ehr.companymanagement.integration.domain.CompanyPaymentDetails;
import com.ehr.companymanagement.integration.domain.CompanySubscriptionMapping;
import com.ehr.companymanagement.integration.domain.SubscriptionPlans;
import com.ehr.companymanagement.integration.domain.SubscriptionType;
import com.ehr.companymanagement.integration.repository.CompanyDetailsRepository;
import com.ehr.companymanagement.integration.repository.CompanyPaymentDetailsRepository;
import com.ehr.companymanagement.integration.repository.CompanySubscriptionMappingRepository;
import com.ehr.companymanagement.integration.repository.SubscriptionPlanRepository;
import com.ehr.companymanagement.util.CustomTransactional;
import com.ehr.core.dto.AddressDTO;
import com.ehr.core.dto.ColumnDetailsDTO;
import com.ehr.core.dto.DocumentDto;
import com.ehr.core.dto.DocumentGetDto;
import com.ehr.core.dto.NotificationRequest;
import com.ehr.core.dto.SequenceColumnDTO;
import com.ehr.core.dto.SuccessResponse;
import com.ehr.core.dto.UserIdListDTO;
import com.ehr.core.dto.UserMasterDTO;
import com.ehr.core.enums.DataOperation;
import com.ehr.core.enums.MasterType;
import com.ehr.core.enums.NotificationType;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.feignclients.SystemServiceFeignProxy;
import com.ehr.core.util.ColumnUtil;
import com.ehr.core.util.Constants;
import com.ehr.core.util.DateUtil;
import com.ehr.core.util.DocumentUtil;
import com.ehr.core.util.EmailUtil;
import com.ehr.core.util.HistoryFeedOperation;
import com.ehr.core.util.HistoryFeedUtil;
import com.ehr.core.util.LocationUtil;
import com.ehr.core.util.LogUtil;
import com.ehr.core.util.ObjectUtils;
import com.ehr.core.util.SvmUtil;
import com.ehr.core.util.UserUtil;
import com.ehr.core.util.ValidatorUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyDetailsServiceImpl implements CompanyDetailsService {

	private static final String ADMIN_ID = "adminId";

	private static final String COMPANY_NOT_FOUND_WITH_ID = "Company not found with id : ";

	private static final String FULL_NAME = "fullName";

	private static final String VALID_TO = "validTo";

	private static final String MOBILE_NO = "mobileNo";

	private static final String VALID_FROM = "validFrom";

	private static final String PLAN_NAME = "planName";

	private static final String EMAIL_ADDRESS = "emailAddress";

	private static final String USER_LAST_NAME = "userLastName";

	private static final String COMPANY_DETAILS = "companyDetails";

	private static final String CLASSNAME = CompanyDetailsServiceImpl.class.getSimpleName();

	private static final List<String> fieldList = Arrays.asList(Constants.USER_FIRST_NAME.toLowerCase(),
			USER_LAST_NAME.toLowerCase(), EMAIL_ADDRESS.toLowerCase(), MOBILE_NO.toLowerCase(), PLAN_NAME.toLowerCase(),
			VALID_FROM.toLowerCase(), VALID_TO.toLowerCase(), FULL_NAME.toLowerCase());

	private final DocumentUtil documentUtil;

	private final SubscriptionPlanRepository subscriptionPlanRepository;

	@PersistenceContext
	private EntityManager em;

	private final ColumnUtil columnUtil;
	
	private final SystemServiceFeignProxy systemServiceFeignProxy;

	private final UserUtil userUtil;

	@Value("${documentDirectory}")
	private String documentDirectory;
	
	private final SvmUtil svmUtil;
	
	private final EmailUtil emailUtil;

	private final CompanyDetailsRepository companyDetailsRepository;

	private final CompanySubscriptionMappingRepository companySubscriptionMappingRepository;
	
	private final CompanyPaymentDetailsRepository companyPaymentDetailsRepository;
	
	private final LocationUtil locationUtil;

	private String whiteLabel = "ECATC004";
	private String coBranding = "ECATC001";
	private String noBranding = "ECATC003";

	@Value("${ipAddress}")
	private String ipAddress;

	@Override
	@CustomTransactional
	public Map<String, Object> saveCompanyDetails(CompanyDetailsDTO companyDto, MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		SuccessResponse success = new SuccessResponse();
		boolean addUser = false;
		boolean isEdit = false;
		Map<String, Object> userResponse = userUtil.tokenVerification(headers);
		try {
			Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
			String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));

			CompanyManagmentValidatorUtil.companyDtoValiadatorUtil(companyDto, language);

			CompanyDetails company = setCompanyDetails(companyDto);
			CompanyDetails oldcompanyDetails = new CompanyDetails();

			validateUniqueCompanyName(companyDto.getCompanyName(), companyDto.getId());

			if (ValidatorUtil.isNumberNullorEmpty(companyDto.getId())) {
				log.info("creating new company details...");
				company.setCreatedBy(userId);
				company.setCreatedOn(new Date());
				success.setMessage("Company added");
				addUser = true;
			} else {
				log.info("updating  company details...");

				company = updateExistingCompany(companyDto, userId, oldcompanyDetails);
				success.setMessage("Company updated");
				isEdit = true;
				userUtil.updateUserMobileNumber(companyDto.getAdminUser(), headers);
			}

			saveCompanyAddressDetails(companyDto, headers, company);
			handleCompanyLogo(companyDto, company);
			handleEmailLogo(companyDto, company);

			company.setEmailAddress(companyDto.getAdminUser().getEmailAddress());
			company.setPlanName(subscriptionPlanRepository.findPlanNameByIdAndIsTrue(companyDto.getSubId()));
			company.setValidFrom(new Date(companyDto.getValidFrom()));
			company.setValidTo(new Date(companyDto.getValidTo()));
			company.setFullName(companyDto.getAdminUser().getUserFirstName() + Constants.SPACE
					+ companyDto.getAdminUser().getUserLastName());

			company = companyDetailsRepository.save(company);

			log.info("Handling history feed updates for company details");
			if (Boolean.TRUE.equals(isEdit)) {
				Long historyFeedId = HistoryFeedUtil.createSaveHistoryFeedObject(systemServiceFeignProxy,
						DataOperation.EDIT.getOperationType(), 0L, MasterType.CompanyDetails.getMasterName(),
						company.getId(), company.getCompanyName(), DataOperation.MANUAL_ENTRY.getOperationType(),
						headers);
				svmUtil.saveChangedField(ObjectUtils.getChangedFields(oldcompanyDetails, company), historyFeedId);
			} else {
				HistoryFeedUtil.createSaveHistoryFeedObject(systemServiceFeignProxy,
						DataOperation.ADD.getOperationType(), 0L, MasterType.CompanyDetails.getMasterName(),
						company.getId(), company.getCompanyName(), DataOperation.MANUAL_ENTRY.getOperationType(),
						headers);
			}

			// add company subscription and payment details
			setAndSaveCompanySubAndPaymentDetails(companyDto, userId, company);

			// add super ADMIN user for the company
			if (Boolean.TRUE.equals(addUser)) {
				log.info("Adding super admin user for the company");
				UserMasterDTO adminUser = companyDto.getAdminUser();
				adminUser.setCompanyId(company.getId());
				adminUser.setLoginId(adminUser.getEmailAddress());
				Long uId = addUser(adminUser, headers, company);
				company.setAdminId(uId);
				companyDetailsRepository.save(company);
			}

			// notification
			sendNotification(addUser, companyDto, company);

			map.put(Constants.COMPANY_ID, company.getId());
			map.put(Constants.SUCCESS, success);
			map.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	@CustomTransactional
	private void saveCompanyAddressDetails(CompanyDetailsDTO companyDto, MultiValueMap<String, String> headers,
			CompanyDetails company) throws BussinessException, ContractException, TechnicalException {
		Long addressId = locationUtil.saveAddressWithoutValidationAndReturnAddressId(companyDto.getAddressDto(),
				headers);
		company.setAddressId(addressId);
	}

	@CustomTransactional
	private void setAndSaveCompanySubAndPaymentDetails(CompanyDetailsDTO companyDto, Long userId,
			CompanyDetails company) throws ContractException {
		CompanySubscriptionMapping companySub = companySubscriptionMappingRepository
				.findByCompanyIdAndSubscriptionPlanIdAndActive(company.getId(), companyDto.getSubId(), Boolean.TRUE);
		if (companySub != null) {
			companySub.setValidFrom(new Date(companyDto.getValidFrom()));
			companySub.setValidTo(new Date(companyDto.getValidTo()));
		} else {
			CompanySubscriptionMapping previousCompanySubscription = companySubscriptionMappingRepository
					.findByCurrentActiveSubscriptionCompanyId(company.getId());
			if (previousCompanySubscription != null) {
				previousCompanySubscription.setActive(Boolean.FALSE);
				companySubscriptionMappingRepository.save(previousCompanySubscription);
			}
			companySub = new CompanySubscriptionMapping();
			companySub.setActive(Boolean.TRUE);
			companySub.setCompanyId(company.getId());
			companySub.setSubscriptionPlanId(company.getSubId());
			companySub.setValidFrom(new Date(companyDto.getValidFrom()));
			companySub.setValidTo(new Date(companyDto.getValidTo()));
		}
		companySubscriptionMappingRepository.save(companySub);

		// company payment details
		if (companyDto.getPaymentDto() != null) {
			CompanyPaymentDetails payment = null;
			if (companyDto.getPaymentDto().getId() != null && companyDto.getPaymentDto().getId() > 0) {
				payment = companyPaymentDetailsRepository.findByIdAndActive(companyDto.getPaymentDto().getId(),
						Boolean.TRUE);
				if (payment == null) {
					log.error("campany details not found with payment id {}", companyDto.getPaymentDto().getId());
					throw new ContractException(HttpStatus.NOT_FOUND, "Company Payment details not found");
				}
				payment.setModifiedBy(userId);
				payment.setModifiedOn(new Date());
			} else {
				payment = new CompanyPaymentDetails();
				payment.setCreatedBy(userId);
				payment.setCreatedOn(new Date());
			}

			payment.setCompanyId(company.getId());
			payment.setSubscriptionPlanId(companyDto.getPaymentDto().getSubscriptionPlanId());
			payment.setExpiryDate(new Date(companyDto.getPaymentDto().getExpiryDate()));
			payment.setNextPaymentDate(new Date((companyDto.getPaymentDto().getNextPaymentDate())));
			payment.setPaymentAmount(companyDto.getPaymentDto().getPaymentAmount());
			payment.setPaymentOn(new Date());

			companyPaymentDetailsRepository.save(payment);
		}
	}

	private void handleCompanyLogo(CompanyDetailsDTO companyDto, CompanyDetails company)
			throws TechnicalException, BussinessException, ContractException {
		try {
			if (companyDto.getLogo() != null && ObjectUtils.isZero(companyDto.getLogo().getId())) {
				DocumentDto documentDto = setDocDTO(companyDto.getLogo());
				Long docMap = documentUtil.addDocument(documentDto);
				company.setLogoId(docMap);
			}
		} catch (Exception e) {
			log.error("Error occured: {}", e.getMessage(), e);
		}
	}

	private void handleEmailLogo(CompanyDetailsDTO companyDto, CompanyDetails company)
			throws TechnicalException, BussinessException, ContractException {
		try {
			if (companyDto.getEmailLogo() != null && companyDto.getEmailLogo().getId() == 0) {
				DocumentDto documentDto = setDocDTO(companyDto.getEmailLogo());
				Long docMap = documentUtil.addDocument(documentDto);
				company.setEmailLogoId(docMap);
			}
		} catch (Exception e) {
			log.error("Error occured: {}", e.getMessage(), e);
		}
	}

	private DocumentDto setDocDTO(DocumentGetDto logo) {
		DocumentDto documentDto = new DocumentDto();
		String[] docuArray = logo.getDocumentName().split("\\.");
		documentDto.setFileName(docuArray[0]);
		documentDto.setExtension(docuArray[1]);
		documentDto.setBase64(logo.getBase64());
		documentDto.setSize("1");
		return documentDto;
	}

	private void sendNotification(boolean addUser, CompanyDetailsDTO companyDto, CompanyDetails company)
			throws BussinessException, ContractException, TechnicalException {
		if (addUser) {
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			executorService.submit(() -> {
				try {
					String path = getCompanyLogoPath(company);
					NotificationRequest notificationRequest = prepareNotificationRequest(companyDto, company, path);
					emailUtil.sendNotification(notificationRequest);
				} catch (Exception | TechnicalException | BussinessException | ContractException e) {
					log.error("error while sending notification... for email {}",
							companyDto.getAdminUser().getEmailAddress());
					log.error(e.getLocalizedMessage());
				}
			});
			executorService.shutdown();
		}
	}

	private String getCompanyLogoPath(CompanyDetails company)
			throws TechnicalException, BussinessException, ContractException {
		String path = "";
		if (company.getLogoId() != null && company.getLogoId() > 0L) {
			DocumentGetDto returnLogoDto = documentUtil.getDocumentById(company.getLogoId());
			if (returnLogoDto != null && StringUtils.isNotEmpty(returnLogoDto.getPath())) {
				path = returnLogoDto.getPath();
			}
		}
		return path;
	}

	private NotificationRequest prepareNotificationRequest(CompanyDetailsDTO companyDto, CompanyDetails company,
			String path) {
		NotificationRequest notificationRequest = new NotificationRequest();
		notificationRequest.setEmailAttachments(new ArrayList<>());
		notificationRequest.setRequestType(NotificationType.EMAIL_NOTIFICATION.getId());
		SubscriptionPlans plans = subscriptionPlanRepository.findBySubscriptionPlanIdAndActive(company.getSubId(),
				Boolean.TRUE);

		Map<String, String[]> senderReceiver = new HashMap<>();
		UserMasterDTO user = companyDto.getAdminUser();
		senderReceiver.put("to", new String[] { user.getEmailAddress() });
		notificationRequest.setSenderReciever(senderReceiver);

		Map<String, String> templateVariables = new HashMap<>();
		templateVariables.put("$companyName", companyDto.getCompanyName());
		templateVariables.put("$name", user.getUserFirstName());
		templateVariables.put("$companyLogoPath", path);
		notificationRequest.setTemplateVariables(templateVariables);

		setTemplateCode(plans, notificationRequest);
		return notificationRequest;
	}

	private void setTemplateCode(SubscriptionPlans plans, NotificationRequest notificationRequest) {
		if (ObjectUtils.isZero(plans.getType())) {
			log.info("triggering email for user is of type : whitelabel");
			notificationRequest.setTemplateCode(whiteLabel);
		} else if (plans.getType().equals(1L)) {
			log.info("triggering email for user is of type : cobranding");
			notificationRequest.setTemplateCode(coBranding);
		} else {
			log.info("triggering email for user is of type : nobranding");
			notificationRequest.setTemplateCode(noBranding);
		}
	}

	@Override
	public Map<String, Object> findCompanyDetailsById(Long id, MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException, IOException {
		Map<String, Object> map = new HashMap<>();
		SuccessResponse success = new SuccessResponse();

		Map<String, Object> userResponse = userUtil.tokenVerification(headers);
		String language = userResponse.get(Constants.USER_LANGUAGE) + Constants.EMPTYSTRING;

		DocumentGetDto logo = null;
		CompanyDetails companyDetails = null;
		CompanyDetailsDTO companyDetailsDTO = new CompanyDetailsDTO();
		log.info(LogUtil.startLog(CLASSNAME));
		try {
			if (ValidatorUtil.isNumberNullorEmpty(id)) {
				log.error(EnglishConstants.ID0001);
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE, ConstantsUtil.getConstant("ID0001", language));
			} else {
				companyDetails = companyDetailsRepository.findByIdAndActive(id, Boolean.TRUE);
				if (companyDetails != null) {
					BeanUtils.copyProperties(companyDetails, companyDetailsDTO);
					UserMasterDTO user = userUtil.getUserById(companyDetails.getAdminId(), headers);
					companyDetailsDTO.setAdminUser(user);

					fetchSubscriptionPlanDetails(companyDetails, companyDetailsDTO);

					if (companyDetails.getCreatedOn() != null) {
						companyDetailsDTO.setCreatedOn(
								DateFormatUtils.format(companyDetails.getCreatedOn(), DateUtil.dd_MM_yyyy));
					}

					if (companyDetails.getModifiedOn() != null) {
						companyDetailsDTO.setModifiedOn(
								DateFormatUtils.format(companyDetails.getModifiedOn(), DateUtil.dd_MM_yyyy));
					}

					fetchCompanySubscriptionDetails(companyDetails, companyDetailsDTO);

					fetchCompanyDetails(id, companyDetailsDTO);

					setCompanyLogoDetails(logo, companyDetails, companyDetailsDTO);

					setCompanyAddressDetails(headers, companyDetails, companyDetailsDTO);

					success.setMessage("company found");
				} else {
					log.error(EnglishConstants.CD0001);
					throw new BussinessException(HttpStatus.NOT_FOUND, ConstantsUtil.getConstant("CD0001", language));
				}
			}
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		map.put(COMPANY_DETAILS, companyDetailsDTO);
		map.put(Constants.COMPANY_ID, companyDetailsDTO.getId());
		map.put(Constants.COMPANY_NAME, companyDetailsDTO.getCompanyName());
		map.put("companyLogo", companyDetailsDTO.getLogo() != null ? companyDetailsDTO.getLogo() : null);
		map.put(Constants.SUCCESS, success);
		map.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	private void setCompanyAddressDetails(MultiValueMap<String, String> headers, CompanyDetails companyDetails,
			CompanyDetailsDTO companyDetailsDTO) throws BussinessException, ContractException, TechnicalException {
		if (companyDetails.getAddressId() != null && companyDetails.getAddressId() > 0) {
			AddressDTO addressDto = locationUtil.findByAddressId(companyDetails.getAddressId(), headers);
			companyDetailsDTO.setAddressDto(addressDto);
		}
	}

	private void fetchSubscriptionPlanDetails(CompanyDetails companyDetails, CompanyDetailsDTO companyDetailsDTO) {
		if (ObjectUtils.isPositiveNonZero(companyDetails.getSubId())) {
			SubscriptionPlans subscriptionPlans = subscriptionPlanRepository
					.findBySubscriptionPlanIdAndActive(companyDetails.getSubId(), Boolean.TRUE);
			if (subscriptionPlans != null) {
				SubscriptionPlansDto subscriptionPlansDto = new SubscriptionPlansDto();
				BeanUtils.copyProperties(subscriptionPlans, subscriptionPlansDto);
				companyDetailsDTO.setSubscriptionPlansDto(subscriptionPlansDto);
			}
		}
	}

	private void fetchCompanySubscriptionDetails(CompanyDetails companyDetails, CompanyDetailsDTO companyDetailsDTO) {
		CompanySubscriptionMapping companySubscriptionMapping = companySubscriptionMappingRepository
				.findByCompanyIdAndSubscriptionPlanIdAndActive(companyDetails.getId(), companyDetails.getSubId(),
						Boolean.TRUE);

		if (companySubscriptionMapping != null) {
			if (companySubscriptionMapping.getValidFrom() != null) {
				companyDetailsDTO.setValidFrom(companySubscriptionMapping.getValidFrom().getTime());
			}

			if (companySubscriptionMapping.getValidTo() != null) {
				companyDetailsDTO.setValidTo(companySubscriptionMapping.getValidTo().getTime());
			}
		}
	}

	private void fetchCompanyDetails(Long id, CompanyDetailsDTO companyDetailsDTO) {
		CompanyPaymentDetails paymentDetails = companyPaymentDetailsRepository.findByCompanyIdAndActivetrue(id);
		if (paymentDetails != null) {
			CompanyPaymentDTO paymentDto = new CompanyPaymentDTO();
			BeanUtils.copyProperties(paymentDetails, paymentDto);
			paymentDto.setNextPaymentDate(
					paymentDetails.getNextPaymentDate() != null ? paymentDetails.getNextPaymentDate().getTime() : null);
			paymentDto.setExpiryDate(
					paymentDetails.getExpiryDate() != null ? paymentDetails.getExpiryDate().getTime() : null);
			companyDetailsDTO.setPaymentDto(paymentDto);
		}
	}

	private void setCompanyLogoDetails(DocumentGetDto logo, CompanyDetails companyDetails,
			CompanyDetailsDTO companyDetailsDTO)
			throws JsonProcessingException, TechnicalException, BussinessException, ContractException {
		if (ObjectUtils.isPositiveNonZero(companyDetails.getLogoId())) {
			logo = documentUtil.getDocumentById(companyDetails.getLogoId());
			if (logo != null && StringUtils.isNotEmpty(logo.getPath())) {
				log.info("fetch company after saving from document service is : " + logo.getPath());
			}
		}
		companyDetailsDTO.setLogo(logo);
	}

	@Override
	public Map<String, Object> findAllCompanyDetailsByPaginationCriteria(List<SequenceColumnDTO> sequenceColumnDTOs,
			int draw, int start, int length, String columns, String search, String sortOrder, String sortField,
			String searchCol, boolean booleanfield, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		if (length == 0) {
			length = Constants.ONE_BILLION;
		}
		Map<String, Object> map = new HashMap<>();
		SuccessResponse success = new SuccessResponse();
		List<CompanyDetailsListingDTO> companyDetailsDTOs = new ArrayList<>();
		List<CompanyDetails> companyDetails = null;
		CompanyDetailsListingDTO companyDetailsDTO = null;
		List<CompanyDetails> saveCompData = new ArrayList<>();
		List<ColumnDetailsDTO> columnDetailsDTOs = new ArrayList<>();
		Long count = 0L;
		userUtil.tokenVerification(headers);
		try {

			PageRequest pageRequest = PageRequest.of(start, length,
					org.springframework.data.domain.Sort.Direction.fromString(sortOrder), sortField);

			Page<CompanyDetails> page = paginateCompanyDetails(search, searchCol, pageRequest, booleanfield, headers);
			if (page != null) {
				companyDetails = page.getContent();
				count = page.getTotalElements();
			}
			if (CollectionUtils.isNotEmpty(companyDetails)) {
				for (CompanyDetails details : companyDetails) {
					companyDetailsDTO = new CompanyDetailsListingDTO();
					BeanUtils.copyProperties(details, companyDetailsDTO);

					if (ObjectUtils.isPositiveNonZero(details.getAdminId())) {
						companyDetailsDTO.setAdminUser(userUtil.getUserById(details.getAdminId(), headers));

					}
					setCompanySubscriptionDetails(companyDetailsDTO, details);

					setCompanyPaymentDetails(companyDetailsDTO, details);

					if (!details.getFullName().equals(companyDetailsDTO.getAdminUser().getFullName())) {
						details.setFullName(companyDetailsDTO.getAdminUser().getFullName());
						companyDetailsRepository.save(details);
					}
					saveCompData.add(details);

					companyDetailsDTOs.add(companyDetailsDTO);

				}
				success.setMessage("companyDetails found");
			}
			if (sequenceColumnDTOs != null) {
				setSequenceColumns(sequenceColumnDTOs, columnDetailsDTOs);
			}

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		map.put(Constants.COUNT, count);
		map.put(Constants.COLUMN, columnDetailsDTOs);
		map.put(COMPANY_DETAILS, companyDetailsDTOs);
		map.put(Constants.SUCCESS, success);
		map.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	private void setSequenceColumns(List<SequenceColumnDTO> sequenceColumnDTOs,
			List<ColumnDetailsDTO> columnDetailsDTOs) throws TechnicalException {
		for (SequenceColumnDTO sequenceColumnDTO : sequenceColumnDTOs) {
			if (ObjectUtils.isPositiveNonZero(sequenceColumnDTO.getColumnID())
					&& ObjectUtils.isPositiveNonZero(sequenceColumnDTO.getSequenceColumn())) {
				ColumnDetailsDTO columnDetailsDTO = columnUtil.findByColumnId(sequenceColumnDTO.getColumnID());
				if (columnDetailsDTO != null) {
					columnDetailsDTOs.add(columnDetailsDTO);
				}
			}
		}
	}

	private void setCompanySubscriptionDetails(CompanyDetailsListingDTO companyDetailsDTO, CompanyDetails details) {
		CompanySubscriptionMapping companySubscription = companySubscriptionMappingRepository
				.findByCurrentActiveSubscriptionCompanyId(details.getId());

		if (companySubscription != null) {
			if (ObjectUtils.isPositiveNonZero(companySubscription.getSubscriptionPlanId())) {

				SubscriptionPlans plans = subscriptionPlanRepository
						.findBySubscriptionPlanIdAndActive(companySubscription.getSubscriptionPlanId(), Boolean.TRUE);

				if (plans != null) {
					SubscriptionPlansDto plansDto = new SubscriptionPlansDto();
					BeanUtils.copyProperties(plans, plansDto);
					companyDetailsDTO.setSubscriptionPlansDto(plansDto);
				} else {
					log.error("No Subscription plan found with given Id : "
							+ companySubscription.getSubscriptionPlanId());
				}
			}
			if (companySubscription.getValidFrom() != null) {
				companyDetailsDTO.setEffectiveFromDate(
						DateFormatUtils.format(companySubscription.getValidFrom().getTime(), DateUtil.dd_MM_yyyy));

			}
			if (companySubscription.getValidTo() != null) {
				companyDetailsDTO.setEffectiveToDate(
						DateFormatUtils.format(companySubscription.getValidTo().getTime(), DateUtil.dd_MM_yyyy));

			}
		}
	}

	private void setCompanyPaymentDetails(CompanyDetailsListingDTO companyDetailsDTO, CompanyDetails details) {
		List<CompanyPaymentDetails> companyPaymentDetails = companyPaymentDetailsRepository
				.findByCompanyIdAndActive(details.getId(), Boolean.TRUE);

		List<CompanyPaymentDTO> paymentList = (companyPaymentDetails != null ? companyPaymentDetails
				: Collections.emptyList()).stream().map(payment -> {
					CompanyPaymentDetails companyPayment = (CompanyPaymentDetails) payment;
					CompanyPaymentDTO dto = new CompanyPaymentDTO();
					BeanUtils.copyProperties(companyPayment, dto);
					dto.setNextPaymentDate(
							companyPayment.getNextPaymentDate() != null ? companyPayment.getNextPaymentDate().getTime()
									: null);
					dto.setExpiryDate(
							companyPayment.getExpiryDate() != null ? companyPayment.getExpiryDate().getTime() : null);
					return dto;
				}).toList();
		companyDetailsDTO.setPaymentDto(paymentList);
	}

	private Page<CompanyDetails> paginateCompanyDetails(String search, String searchCol, PageRequest pageRequest,
			boolean booleanfield, MultiValueMap<String, String> headers) throws TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CompanyDetails> criteria = builder.createQuery(CompanyDetails.class);
		Root<CompanyDetails> root = criteria.from(CompanyDetails.class);
		List<Predicate> predicates = new ArrayList<>();
		Predicate activepredicate = null;
		Predicate searchpredicatevar = null;
		Predicate companyIdPredicate = null;
		activepredicate = builder.equal(root.get(Constants.ACTIVE), booleanfield);
		List<Long> compIds = new ArrayList<>();
		List<Long> subscriptionIds = subscriptionPlanRepository.findAllSubscriptionPlansIds();
		if (CollectionUtils.isNotEmpty(subscriptionIds)) {
			fetchAndsetCompanySubDetails(booleanfield, compIds, subscriptionIds);
		}
		if (CollectionUtils.isNotEmpty(compIds)) {
			Expression<Long> ex1 = root.get(Constants.ID);
			companyIdPredicate = ex1.in(compIds);
		}

		if (StringUtils.isNotEmpty(searchCol) && !fieldList.contains(searchCol.toLowerCase())) {
			searchpredicatevar = builder.like(builder.lower(root.get(searchCol)),
					Constants.LIKE_STRING.replace(Constants.LIKE, search.toLowerCase()));
		} else {
			if (StringUtils.equalsIgnoreCase(searchCol, PLAN_NAME)) {
				searchpredicatevar = searchPredicateBySubscriptionPlanName(search, root);
			}
			if (searchCol.equals(VALID_FROM) || searchCol.equals(VALID_TO)) {
				searchpredicatevar = addDatePredicate(search, searchCol, root, searchpredicatevar);
			}
			if (StringUtils.equalsIgnoreCase(searchCol, Constants.USER_FIRST_NAME)
					|| StringUtils.equalsIgnoreCase(searchCol, USER_LAST_NAME)
					|| StringUtils.equalsIgnoreCase(searchCol, EMAIL_ADDRESS)
					|| StringUtils.equalsIgnoreCase(searchCol, MOBILE_NO)
					|| StringUtils.equalsIgnoreCase(searchCol, FULL_NAME)) {

				searchpredicatevar = buildSearchPredicate(search, searchCol, booleanfield, headers, root);
			}
		}
		if (companyIdPredicate != null) {
			addSeachColumnCiteria(searchCol, builder, criteria, predicates, activepredicate, searchpredicatevar,
					companyIdPredicate);
		} else {
			addSearchCiteria(search, builder, criteria, predicates, activepredicate, searchpredicatevar);
		}

		criteria.orderBy(QueryUtils.toOrders(pageRequest.getSort(), root, builder));
		List<CompanyDetails> result = em.createQuery(criteria).setFirstResult((int) pageRequest.getOffset())
				.setMaxResults(pageRequest.getPageSize()).getResultList();
		List<CompanyDetails> result1 = em.createQuery(criteria).getResultList();
		Integer count = result1.size();
		log.info(LogUtil.exitLog(CLASSNAME));
		return new PageImpl<>(result, pageRequest, count);
	}

	private Predicate buildSearchPredicate(String search, String searchCol, boolean booleanfield,
			MultiValueMap<String, String> headers, Root<CompanyDetails> root) {
		List<Long> list;
		if (booleanfield) {
			list = companyDetailsRepository.findAllAdminId();
		} else {
			list = companyDetailsRepository.findAllAdminIdInToInActiveCompany();
		}

		UserIdListDTO userResponse = userUtil.userListForSearch(list, searchCol, search, headers);

		List<Long> userIds = userResponse.getUserIds();
		Predicate predicate;

		if (CollectionUtils.isNotEmpty(userIds)) {
			Expression<Long> ex = root.get(ADMIN_ID);
			predicate = ex.in(userIds);
		} else {
			predicate = root.get(ADMIN_ID).in(-1L);
		}
		return predicate;
	}

	private void addSearchCiteria(String search, CriteriaBuilder builder, CriteriaQuery<CompanyDetails> criteria,
			List<Predicate> predicates, Predicate activepredicate, Predicate searchpredicatevar) {
		if (StringUtils.isNotEmpty(search)) {
			if (searchpredicatevar != null) {
				criteria.where(builder.and(searchpredicatevar, activepredicate));
			} else {
				searchpredicatevar = builder.and(predicates.toArray(new Predicate[predicates.size()]));
				criteria.where(builder.and(searchpredicatevar, activepredicate));
			}
		} else {
			criteria.where(builder.and(activepredicate));
		}
	}

	private void addSeachColumnCiteria(String searchCol, CriteriaBuilder builder,
			CriteriaQuery<CompanyDetails> criteria, List<Predicate> predicates, Predicate activepredicate,
			Predicate searchpredicatevar, Predicate companyIdPredicate) {
		if (StringUtils.isNotEmpty(searchCol)) {
			if (searchpredicatevar != null) {
				criteria.where(builder.and(searchpredicatevar, activepredicate, companyIdPredicate));
			} else {
				searchpredicatevar = builder.and(predicates.toArray(new Predicate[predicates.size()]));
				criteria.where(builder.and(searchpredicatevar, activepredicate, companyIdPredicate));
			}

		} else {
			criteria.where(builder.and(activepredicate, companyIdPredicate));
		}
	}

	private Predicate searchPredicateBySubscriptionPlanName(String search, Root<CompanyDetails> root) {
		List<SubscriptionPlans> planNames = subscriptionPlanRepository.findByPlanName(search);
		List<Long> collect = planNames.stream().map(SubscriptionPlans::getSubscriptionPlanId).toList();
		List<Long> companyIds = null;
		if (CollectionUtils.isNotEmpty(collect)) {
			companyIds = companySubscriptionMappingRepository.findbyListOfSubscriptionPlanId(collect);
		}
		Predicate predicate;
		if (CollectionUtils.isNotEmpty(companyIds)) {
			Expression<Long> ex = root.get(Constants.ID);
			predicate = ex.in(companyIds);
		} else {
			predicate = root.get(Constants.ID).in(-1L);
		}
		return predicate;
	}

	private void fetchAndsetCompanySubDetails(boolean booleanfield, List<Long> compIds, List<Long> subscriptionIds) {
		List<CompanySubscriptionMapping> companySubscriptions;
		companySubscriptions = companySubscriptionMappingRepository.findCompanysBySubscriptions(subscriptionIds);
		if (CollectionUtils.isNotEmpty(companySubscriptions)) {
			for (CompanySubscriptionMapping cs : companySubscriptions) {
				if (cs.getValidTo() != null && cs.getValidFrom() != null && booleanfield
						&& cs.getValidFrom().before(new Date()) && cs.getValidTo().after(new Date())) {
					compIds.add(cs.getCompanyId());
				}
			}
		}
	}

	@CustomTransactional
	Long addUser(UserMasterDTO adminUser, MultiValueMap<String, String> headers, CompanyDetails company)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		List<Long> roles = new ArrayList<>();
		roles.add(8L);
		adminUser.setRoles(roles);

		if (adminUser.getCurrency() != null) {
			adminUser.setCurrency(svmUtil.findById(company.getCurrency()).getValue());
		}

		if (company.getTimeZone() != null) {
			adminUser.setTimeZone(svmUtil.findById(company.getTimeZone()).getValue());
		}

		if (company.getLanguage() != null) {
			adminUser.setLanguage(svmUtil.findById(company.getLanguage()).getValue());
		}

		try {
			return userUtil.addUserAndReturnUserId(adminUser, headers);
		} catch (TechnicalException t) {
			companyDetailsRepository.delete(companyDetailsRepository.findByIdAndActive(adminUser.getCompanyId()));
			throw new TechnicalException(t.getDescription(), t.getErrorcode());
		} catch (BussinessException b) {
			companyDetailsRepository.delete(companyDetailsRepository.findByIdAndActive(adminUser.getCompanyId()));
			throw new BussinessException(b.getErrorcode(), b.getDescription());
		} catch (ContractException c) {
			companyDetailsRepository.delete(companyDetailsRepository.findByIdAndActive(adminUser.getCompanyId()));
			throw new ContractException(c.getErrorcode(), c.getDescription());
		}
	}

	@Override
	public Map<String, Object> getCompanySubscriptionPlainValidity(MultiValueMap<String, String> headers)
			throws TechnicalException, ContractException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> userResponse = userUtil.tokenVerification(headers);
		Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));
		Map<String, Object> response = new HashMap<>();
		try {
			log.info("Validating companyId: {}", companyId);
			CompanyManagmentValidatorUtil.idValidator(companyId);

			CompanyDetails companyDetails = companyDetailsRepository.findByIdAndActive(companyId, Boolean.TRUE);

			if (companyDetails == null) {
				log.error("Company details not found for companyId: {}", companyId);
				log.error(EnglishConstants.CD0001);
				throw new BussinessException(HttpStatus.NOT_FOUND, EnglishConstants.CD0001);
			}
			log.info("Fetched company details: {}", companyDetails);
			CompanySubscriptionMapping companySubscription = companySubscriptionMappingRepository
					.findByCompanyIdAndSubscriptionPlanIdAndActive(companyDetails.getId(), companyDetails.getSubId(),
							Boolean.TRUE);

			if (companySubscription == null) {
				log.error("Company subscription not found for companyId: {} and subId: {}", companyDetails.getId(),
						companyDetails.getSubId());
				log.error(EnglishConstants.CS0001);
				throw new BussinessException(HttpStatus.NOT_FOUND, EnglishConstants.CS0001);
			}
			Date currentDate = new Date();

			if (companySubscription.getValidFrom().equals(currentDate)
					&& companySubscription.getValidTo().before(currentDate)) {
				log.info("Company subscription is valid for today");
				response.put(Constants.STATUS, Boolean.TRUE);
			} else {
				log.info("Company subscription is not valid for today");
				response.put(Constants.STATUS, Boolean.FALSE);
			}

		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error("Exception encountered in getCompanySubscriptionPlainValidity", e);
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}

		log.info("Returning response from getCompanySubscriptionPlainValidity: {}", response);
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	@Override
	public Map<String, Object> getLogoByCompanyId(Long id)
			throws TechnicalException, ContractException, BussinessException, IOException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> response = new HashMap<>();
		DocumentGetDto logo = null;
		String path = Constants.EMPTYSTRING;
		try {
			CompanyManagmentValidatorUtil.idValidator(id);
			CompanyDetails companyDetails = companyDetailsRepository.findByIdAndActive(id, Boolean.TRUE);
			throwCompanyDetailsNullError(companyDetails);

			log.info("fetching FirstTime User's subscriptionType by given companyId : " + id);
			fetchSubscriptionDetails(response, companyDetails);

			log.info("fetching company logo details from company id");
			if (companyDetails.getLogoId() != null) {
				logo = documentUtil.getDocumentById(companyDetails.getLogoId());
				if (logo != null && StringUtils.isNotEmpty(logo.getPath())) {
					path = logo.getPath();
					log.info("fetch company after saving from document service is : " + path);
				}
			}
			response.put("companyLogo", logo);
			response.put("path", path);
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	private void throwCompanyDetailsNullError(CompanyDetails companyDetails) throws BussinessException {
		if (companyDetails == null) {
			log.error(EnglishConstants.CD0001);
			throw new BussinessException(HttpStatus.NOT_FOUND, EnglishConstants.CD0001);
		}
	}

	private void fetchSubscriptionDetails(Map<String, Object> response, CompanyDetails companyDetails) {
		if (ObjectUtils.isPositiveNonZero(companyDetails.getSubId())) {
			SubscriptionPlans subscriptionPlan = subscriptionPlanRepository
					.findBySubscriptionPlanIdAndActive(companyDetails.getSubId(), Boolean.TRUE);
			if (subscriptionPlan != null) {
				log.info("subscriptionPlan found with given Id : " + companyDetails.getSubId());
				if (subscriptionPlan.getType() != null) {
					log.info("subscription plan type found with the given subscription plan id : "
							+ companyDetails.getSubId() + Constants.SPACE + " is " + subscriptionPlan.getType());
					response.put(Constants.TYPE, subscriptionPlan.getType());
					response.put(Constants.TYPE_NAME, SubscriptionType.getTypeName(subscriptionPlan.getType()));
				}
			}

		}
	}

	@Override
	public Map<String, Object> getAllCompanyDetailsPaginationByEffectiveFromAndTill(
			List<SequenceColumnDTO> sequenceColumnDTOs, int draw, int start, int length, String columns, String search,
			String sortOrder, String sortField, String searchCol, boolean isEffective,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		if (length == 0)
			length = Constants.ONE_BILLION;
		Map<String, Object> map = new HashMap<>();
		SuccessResponse success = new SuccessResponse();
		List<CompanyDetailsDTO> companyDetailsDTOs = new ArrayList<>();
		List<CompanyDetails> companyDetails = null;
		List<CompanyDetails> saveCompData = new ArrayList<>();
		List<ColumnDetailsDTO> columnDetailsDTOs = new ArrayList<>();
		Long count = 0L;
		userUtil.tokenVerification(headers);

		try {

			PageRequest pageRequest = PageRequest.of(start, length,
					org.springframework.data.domain.Sort.Direction.fromString(sortOrder), sortField);

			Page<CompanyDetails> page = paginateByEffectiveFromAndTill(search, searchCol, pageRequest, headers,
					isEffective);
			if (page != null) {
				companyDetails = page.getContent();
				count = page.getTotalElements();
			}
			if (companyDetails != null) {
				for (CompanyDetails details : companyDetails) {
					if (details != null && details.getActive()) {
						processAndPopulateCompanyDetails(headers, companyDetailsDTOs, saveCompData, details);
					}
				}
				success.setMessage("companyDetails found");
			}
			if (CollectionUtils.isNotEmpty(sequenceColumnDTOs)) {
				setSequenceColumns(sequenceColumnDTOs, columnDetailsDTOs);
			}
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}

		if (CollectionUtils.isNotEmpty(saveCompData)) {
			companyDetailsRepository.saveAll(saveCompData);
		}

		map.put(Constants.COUNT, count);
		map.put(Constants.COLUMN, columnDetailsDTOs);
		map.put(COMPANY_DETAILS, companyDetailsDTOs);
		map.put(Constants.SUCCESS, success);
		map.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	private void processAndPopulateCompanyDetails(MultiValueMap<String, String> headers,
			List<CompanyDetailsDTO> companyDetailsDTOs, List<CompanyDetails> saveCompData, CompanyDetails details) {
		CompanyDetailsDTO companyDetailsDTO;
		companyDetailsDTO = new CompanyDetailsDTO();
		BeanUtils.copyProperties(details, companyDetailsDTO);

		if (ObjectUtils.isPositiveNonZero(details.getAdminId())) {
			companyDetailsDTO.setAdminUser(userUtil.getUserById(details.getAdminId(), headers));
		}

		if (!details.getFullName().equals(companyDetailsDTO.getAdminUser().getFullName())) {
			details.setFullName(companyDetailsDTO.getAdminUser().getFullName());
			companyDetailsRepository.save(details);
		}

		CompanySubscriptionMapping companySubscription = companySubscriptionMappingRepository
				.findByCompanyIdAndActive(details.getId(), Boolean.TRUE);

		if (companySubscription != null) {
			populateCompanySubscriptionDetails(companyDetailsDTO, companySubscription);
		}
		saveCompData.add(details);
		companyDetailsDTOs.add(companyDetailsDTO);
	}

	private void populateCompanySubscriptionDetails(CompanyDetailsDTO companyDetailsDTO,
			CompanySubscriptionMapping companySubscription) {
		if (ObjectUtils.isPositiveNonZero(companySubscription.getSubscriptionPlanId())) {

			SubscriptionPlans plans = subscriptionPlanRepository
					.findBySubscriptionPlanIdAndActive(companySubscription.getSubscriptionPlanId(), Boolean.TRUE);

			if (plans != null) {
				SubscriptionPlansDto plansDto = new SubscriptionPlansDto();
				BeanUtils.copyProperties(plans, plansDto);
				companyDetailsDTO.setSubscriptionPlansDto(plansDto);
			} else {
				log.error("No Subscription plan found with given Id : " + companySubscription.getSubscriptionPlanId());
			}
		}
		if (companySubscription.getValidFrom() != null) {
			companyDetailsDTO.setEffectiveFromDate(
					DateFormatUtils.format(companySubscription.getValidFrom().getTime(), DateUtil.dd_MM_yyyy));
		}
		if (companySubscription.getValidTo() != null) {

			companyDetailsDTO.setEffectiveToDate(
					DateFormatUtils.format(companySubscription.getValidTo().getTime(), DateUtil.dd_MM_yyyy));
		}
	}

	private Page<CompanyDetails> paginateByEffectiveFromAndTill(String search, String searchCol,
			PageRequest pageRequest, MultiValueMap<String, String> headers, boolean isEffective) {
		log.info(LogUtil.startLog(CLASSNAME));
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CompanyDetails> criteria = builder.createQuery(CompanyDetails.class);
		Root<CompanyDetails> root = criteria.from(CompanyDetails.class);
		List<Predicate> predicates = new ArrayList<>();
		Predicate companyIdPredicate = null;
		Predicate searchpredicatevar = null;
		List<Long> companyIds = new ArrayList<>();
		if (isEffective) {
			List<Long> subscriptionIds = subscriptionPlanRepository.findAllSubscriptionPlansIds();
			if (CollectionUtils.isNotEmpty(subscriptionIds)) {
				filterCompaniesByValidSubscriptionDates(companyIds, subscriptionIds);

			}
			if (CollectionUtils.isEmpty(companyIds)) {
				return new PageImpl<>(new ArrayList<>(), pageRequest, 0L);
			}
			Expression<Long> ex = root.get(Constants.ID);
			companyIdPredicate = ex.in(companyIds);
		} else {
			companyIds = new ArrayList<>();
			List<Long> subscriptionIds = subscriptionPlanRepository.findAllSubscriptionPlansIds();
			if (subscriptionIds != null && !subscriptionIds.isEmpty()) {
				filterCompaniesWithExpiredSubscriptions(companyIds, subscriptionIds);

			}
			if (CollectionUtils.isEmpty(companyIds)) {
				return new PageImpl<>(new ArrayList<>(), pageRequest, 0L);
			}
			Expression<Long> ex = root.get(Constants.ID);
			companyIdPredicate = ex.in(companyIds);

		}
		searchpredicatevar = applySearchFilter(search, searchCol, headers, builder, root, searchpredicatevar);

		if (companyIdPredicate != null) {
			applySearchAndCompanyFilters(search, builder, criteria, predicates, companyIdPredicate, searchpredicatevar);
		}

		criteria.orderBy(QueryUtils.toOrders(pageRequest.getSort(), root, builder));
		List<CompanyDetails> result = em.createQuery(criteria).setFirstResult((int) pageRequest.getOffset())
				.setMaxResults(pageRequest.getPageSize()).getResultList();
		List<CompanyDetails> result1 = em.createQuery(criteria).getResultList();
		Integer count = result1.size();
		log.info(LogUtil.exitLog(CLASSNAME));
		return new PageImpl<>(result, pageRequest, count);
	}

	private Predicate applySearchFilter(String search, String searchCol, MultiValueMap<String, String> headers,
			CriteriaBuilder builder, Root<CompanyDetails> root, Predicate searchpredicatevar) {
		if (StringUtils.isNotEmpty(searchCol) && !(StringUtils.equalsIgnoreCase(searchCol, Constants.USER_FIRST_NAME)
				|| StringUtils.equalsIgnoreCase(searchCol, USER_LAST_NAME)
				|| StringUtils.equalsIgnoreCase(searchCol, EMAIL_ADDRESS)
				|| StringUtils.equalsIgnoreCase(searchCol, MOBILE_NO)
				|| StringUtils.equalsIgnoreCase(searchCol, PLAN_NAME)
				|| StringUtils.equalsIgnoreCase(searchCol, VALID_FROM)
				|| StringUtils.equalsIgnoreCase(searchCol, VALID_TO)
				|| StringUtils.equalsIgnoreCase(searchCol, FULL_NAME))) {
			searchpredicatevar = builder.like(builder.lower(root.get(searchCol)),
					Constants.LIKE_STRING.replace(Constants.LIKE, search.toLowerCase()));
		} else {
			searchpredicatevar = buildSearchPredicates(search, searchCol, headers, root, searchpredicatevar);
		}
		return searchpredicatevar;
	}

	private void applySearchAndCompanyFilters(String search, CriteriaBuilder builder,
			CriteriaQuery<CompanyDetails> criteria, List<Predicate> predicates, Predicate companyIdPredicate,
			Predicate searchpredicatevar) {
		if (StringUtils.isNotEmpty(search)) {
			if (searchpredicatevar != null) {
				criteria.where(builder.and(searchpredicatevar, companyIdPredicate));
			} else {
				searchpredicatevar = builder.and(predicates.toArray(new Predicate[predicates.size()]));
				criteria.where(builder.and(searchpredicatevar, companyIdPredicate));
			}

		} else {
			criteria.where(companyIdPredicate);
		}
	}

	private Predicate buildSearchPredicates(String search, String searchCol, MultiValueMap<String, String> headers,
			Root<CompanyDetails> root, Predicate searchpredicatevar) {
		if (StringUtils.equalsIgnoreCase(searchCol, PLAN_NAME)) {
			List<SubscriptionPlans> planNames = subscriptionPlanRepository.findByPlanName(search);
			List<Long> collect = planNames.stream().map(SubscriptionPlans::getSubscriptionPlanId).toList();
			List<Long> compIds = null;
			if (collect != null && !collect.isEmpty()) {
				compIds = companySubscriptionMappingRepository.findbyListOfSubscriptionPlanId(collect);
			}
			if (compIds != null && !compIds.isEmpty()) {
				Expression<Long> ex = root.get(Constants.ID);
				searchpredicatevar = ex.in(compIds);
			} else {
				searchpredicatevar = root.get(Constants.ID).in(-1L);
			}
		}

		if (searchCol.equals(VALID_FROM) || searchCol.equals(VALID_TO)) {
			searchpredicatevar = addDatePredicate(search, searchCol, root, searchpredicatevar);
		}

		if (StringUtils.equalsIgnoreCase(searchCol, Constants.USER_FIRST_NAME)
				|| StringUtils.equalsIgnoreCase(searchCol, USER_LAST_NAME)
				|| StringUtils.equalsIgnoreCase(searchCol, EMAIL_ADDRESS)
				|| StringUtils.equalsIgnoreCase(searchCol, MOBILE_NO)
				|| StringUtils.equalsIgnoreCase(searchCol, FULL_NAME)) {

			List<Long> list = companyDetailsRepository.findAllAdminId();

			UserIdListDTO userResponse = userUtil.userListForSearch(list, searchCol, search, headers);

			List<Long> userIds = userResponse.getUserIds();

			if (CollectionUtils.isNotEmpty(userIds)) {
				Expression<Long> ex = root.get(ADMIN_ID);
				searchpredicatevar = ex.in(userIds);
			} else {
				searchpredicatevar = root.get(ADMIN_ID).in(-1L);
			}

		}
		return searchpredicatevar;
	}

	private Predicate addDatePredicate(String search, String searchCol, Root<CompanyDetails> root,
			Predicate searchpredicatevar) {
		try {
			Date startDate = new Date(Long.parseLong(search));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(startDate);

			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			startDate = calendar.getTime();

			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			calendar.set(Calendar.MILLISECOND, 999);
			Date endDate = calendar.getTime();

			List<Long> compIds = searchCol.equals(VALID_FROM)
					? companySubscriptionMappingRepository.findCompanyIdsByValidFromBetween(startDate, endDate)
					: companySubscriptionMappingRepository.findCompanyIdsByValidToBetween(startDate, endDate);

			if (CollectionUtils.isEmpty(compIds)) {
				return root.get(Constants.ID).in(-1);
			}
			Expression<Long> ex = root.get(Constants.ID);
			searchpredicatevar = ex.in(compIds);
		} catch (NumberFormatException e) {
			log.error("Invalid timestamp format: " + search);
			return root.get(Constants.ID).in(-1);
		}
		return searchpredicatevar;
	}

	private void filterCompaniesWithExpiredSubscriptions(List<Long> companyIds, List<Long> subscriptionIds) {
		List<CompanySubscriptionMapping> companySubscriptions;
		companySubscriptions = companySubscriptionMappingRepository.findCompanysBySubscriptions(subscriptionIds);
		if (CollectionUtils.isNotEmpty(companySubscriptions)) {
			for (CompanySubscriptionMapping cs : companySubscriptions) {
				if (cs.getValidTo() != null && cs.getValidTo().before(new Date())) {
					log.error("comparing date of validTill and current date :");
					log.error(
							"valid date with format : " + DateFormatUtils.format(cs.getValidTo(), DateUtil.dd_MM_yyyy));
					log.error("current date with format : " + DateFormatUtils.format(new Date(), DateUtil.dd_MM_yyyy));
					log.error("after condition apply for validation : " + cs.getValidTo().before(new Date()));
					companyIds.add(cs.getCompanyId());
				}
			}
		}
	}

	private void filterCompaniesByValidSubscriptionDates(List<Long> companyIds, List<Long> subscriptionIds) {
		List<CompanySubscriptionMapping> companySubscriptions;
		companySubscriptions = companySubscriptionMappingRepository.findCompanysBySubscriptions(subscriptionIds);
		if (CollectionUtils.isNotEmpty(companySubscriptions)) {
			for (CompanySubscriptionMapping cs : companySubscriptions) {
				if (cs.getValidFrom() != null && cs.getValidFrom().after(new Date())) {
					log.error("comparing date of validFrom and current date :");
					log.error("valid date with format : "
							+ DateFormatUtils.format(cs.getValidFrom(), DateUtil.dd_MM_yyyy));
					log.error("current date with format : " + DateFormatUtils.format(new Date(), DateUtil.dd_MM_yyyy));
					log.error("after condition apply for validation : " + cs.getValidFrom().after(new Date()));
					companyIds.add(cs.getCompanyId());
				}
			}
		}
	}

	@Override
	public Map<String, Object> getCompanySubscriptionPlanStatus(Long id) throws TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> response = new HashMap<>();
		Boolean status = Boolean.FALSE;
		try {
			CompanyDetails companyDetails = companyDetailsRepository.findByIdAndActive(id, Boolean.TRUE);
			status = (companyDetails == null);

			if (companyDetails != null) {
				CompanySubscriptionMapping companySubscription = companySubscriptionMappingRepository
						.findByCompanyIdAndActive(id, Boolean.TRUE);
				if (companySubscription.getValidFrom().after(new Date())) {
					status = Boolean.TRUE;
				}
				if (companySubscription.getValidTo().before(new Date())) {
					status = Boolean.TRUE;
				}
			}
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		response.put(Constants.STATUS, status);
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	@Override
	public Map<String, Object> findCountryIdByCompanyId(Long companyId, MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException, IOException {
		Map<String, Object> map = new HashMap<>();
		userUtil.tokenVerification(headers);
		if (ObjectUtils.isPositiveNonZero(companyId)) {
			Long addressId = companyDetailsRepository.findAddressIdByCompanyId(companyId);
			if (addressId != null) {
				AddressDTO addressdto = locationUtil.findByAddressId(addressId, headers);
				map.put(Constants.COUNTRY_ID, addressdto.getCountryId());
			}
		}
		return map;
	}

	@Override
	public Map<String, Object> validateCompanyId(Long companyId, MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		userUtil.tokenVerification(headers);
		try {
			log.info("Company Id : " + companyId);
			boolean doesExists = companyDetailsRepository.existsByIdAndActive(companyId, Boolean.TRUE);
			if (!doesExists) {
				log.error(COMPANY_NOT_FOUND_WITH_ID + companyId);
				throw new BussinessException(HttpStatus.NOT_FOUND, COMPANY_NOT_FOUND_WITH_ID + companyId, "companyId");
			}
			map.put(Constants.SUCCESS, new SuccessResponse("Company Found"));
			map.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	@Override
	public Map<String, Object> getCompanyNameByCompanyId(Long companyId)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		CompanyDetails companyDetails = companyDetailsRepository.findByIdAndActive(companyId);

		if (companyDetails != null) {
			map.put(Constants.COMPANY_NAME, companyDetails.getCompanyName());
		} else {
			throw new BussinessException(HttpStatus.NOT_FOUND, COMPANY_NOT_FOUND_WITH_ID + companyId, "companyId");
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	private void validateUniqueCompanyName(String companyName, Long companyId) throws ContractException {
		CompanyDetails existingCompany = companyDetailsRepository.findByCompanyNameAndActive(companyName, Boolean.TRUE);
		if (existingCompany != null && !existingCompany.getId().equals(companyId)) {
			throw new ContractException(HttpStatus.UNPROCESSABLE_ENTITY, "Company Name already exists");
		}
	}

	private CompanyDetails setCompanyDetails(CompanyDetailsDTO companyDTO) {
		CompanyDetails company = new CompanyDetails();
		BeanUtils.copyProperties(companyDTO, company);
		return company;
	}

	private CompanyDetails updateExistingCompany(CompanyDetailsDTO companyDTO, Long userId,
			CompanyDetails oldCompanyDetails) throws ContractException {
		CompanyDetails company = companyDetailsRepository.findByIdAndActive(companyDTO.getId(), Boolean.TRUE);
		if (company == null) {
			throw new ContractException(HttpStatus.NOT_FOUND, "Company details not found");
		}
		BeanUtils.copyProperties(company, oldCompanyDetails);
		BeanUtils.copyProperties(companyDTO, company);
		company.setModifiedBy(userId);
		company.setModifiedOn(new Date());
		return company;
	}

	@Override
	@CustomTransactional
	public Map<String, Object> deleteCompanyById(Long id, boolean status, MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		Map<String, Object> map = new HashMap<>();

		Map<String, Object> userResponse = userUtil.tokenVerification(headers);
		String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));
		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
		Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

		SuccessResponse success = new SuccessResponse();
		log.info(LogUtil.startLog(CLASSNAME));
		try {
			if (Boolean.TRUE.equals(CompanyManagmentValidatorUtil.idValidator(id, language))) {
				Optional<CompanyDetails> optionalCompanyDetails = companyDetailsRepository.findById(id);
				if (optionalCompanyDetails.isEmpty()) {
					log.error(EnglishConstants.CD0001);
					throw new BussinessException(HttpStatus.NOT_FOUND, ConstantsUtil.getConstant("CD0001", language));
				}

				CompanyDetails companyDetails = optionalCompanyDetails.get();
				companyDetails.setActive(status);
				companyDetails.setModifiedBy(userId);
				companyDetails.setModifiedOn(new Date());
				companyDetails = companyDetailsRepository.save(companyDetails);

				HistoryFeedUtil.createSaveHistoryFeedObject(systemServiceFeignProxy,
						HistoryFeedOperation.saveActiveDeleteOperation(status), 0L,
						MasterType.CompanyDetails.getMasterName(), companyId, companyDetails.getCompanyName(),
						DataOperation.MANUAL_ENTRY.getOperationType(), headers);
				success.setMessage(Constants.SUCCESS);
			}
			map.put(Constants.SUCCESS, success);
			map.put(Constants.ERROR, null);
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	@Override
	public Map<String, Object> findCompanyNameById(Long id, MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> userResponse = userUtil.tokenVerification(headers);
		String language = userResponse.get(Constants.USER_LANGUAGE) + Constants.EMPTYSTRING;

		CompanyDetails companyDetails = null;
		CompanyDetailsDTO companyDetailsDTO = new CompanyDetailsDTO();
		log.info(LogUtil.startLog(CLASSNAME));
		try {
			if (ValidatorUtil.isNumberNullorEmpty(id)) {
				log.error(EnglishConstants.ID0001);
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE, ConstantsUtil.getConstant("ID0001", language));
			} else {
				companyDetails = companyDetailsRepository.findByIdAndActive(id, Boolean.TRUE);
				if (companyDetails != null) {
					BeanUtils.copyProperties(companyDetails, companyDetailsDTO);
				}
			}
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		map.put(Constants.COMPANY_ID, companyDetailsDTO.getId());
		map.put(Constants.COMPANY_NAME, companyDetailsDTO.getCompanyName());
		map.put(Constants.SUCCESS, new SuccessResponse("Company Found"));
		map.put(Constants.ERROR, null);
		return map;
	}
}
