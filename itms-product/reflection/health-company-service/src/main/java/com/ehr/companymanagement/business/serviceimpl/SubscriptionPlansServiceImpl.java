package com.ehr.companymanagement.business.serviceimpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
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
import com.ehr.companymanagement.business.dto.SubscriptionPlansDto;
import com.ehr.companymanagement.business.service.CompanyDetailsService;
import com.ehr.companymanagement.business.service.SubscriptionPlansService;
import com.ehr.companymanagement.business.validation.CompanyManagmentValidatorUtil;
import com.ehr.companymanagement.business.validation.EnglishConstants;
import com.ehr.companymanagement.integration.domain.CompanyDetails;
import com.ehr.companymanagement.integration.domain.SubscriptionPlans;
import com.ehr.companymanagement.integration.domain.SubscriptionType;
import com.ehr.companymanagement.integration.repository.CompanyDetailsRepository;
import com.ehr.companymanagement.integration.repository.SubscriptionPlanRepository;
import com.ehr.core.dto.ColumnDetailsDTO;
import com.ehr.core.dto.SequenceColumnDTO;
import com.ehr.core.dto.SuccessResponse;
import com.ehr.core.enums.DataOperation;
import com.ehr.core.enums.MasterType;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.feignclients.SystemServiceFeignProxy;
import com.ehr.core.util.ColumnUtil;
import com.ehr.core.util.Constants;
import com.ehr.core.util.DateUtil;
import com.ehr.core.util.HistoryFeedOperation;
import com.ehr.core.util.HistoryFeedUtil;
import com.ehr.core.util.LogUtil;
import com.ehr.core.util.ObjectUtils;
import com.ehr.core.util.SvmUtil;
import com.ehr.core.util.UserUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionPlansServiceImpl implements SubscriptionPlansService {

	private static final String COMPANY_DETAILS = "companyDetails";

	private static final String CLASSNAME = SubscriptionPlansServiceImpl.class.getSimpleName();

	Map<String, Object> response = null;

	
	private final SubscriptionPlanRepository subscriptionPlanRepository;

	
	private final SystemServiceFeignProxy systemServiceFeignProxy;

	@PersistenceContext
	private EntityManager em;

	
	private final ColumnUtil columnUtil;

	
	private final CompanyDetailsService companyDetailsService;

	
	private final CompanyDetailsRepository companyDetailsRepository;

	
	private final UserUtil userUtil;
	
	
	private final SvmUtil svmUtil;

	@Override
	public Map<String, Object> saveSubscriptionPlans(SubscriptionPlansDto subscriptionPlansDto,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));
		response = new HashMap<>();
		Map<String, Object> userResponse = userUtil.tokenVerification(headers);
		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
		String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));
		Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

		SuccessResponse success = new SuccessResponse();
		SubscriptionPlans subscriptionPlans = null;
		SubscriptionPlans subscriptionPlansOld = null;
		Long historyFeedId = 0L;
		try {
			if (Boolean.TRUE.equals(
					CompanyManagmentValidatorUtil.subscriptionPlansDtoValidationUtil(subscriptionPlansDto, language))) {
				if (Boolean.TRUE.equals(CompanyManagmentValidatorUtil
						.isPrimaryKeyPresent((subscriptionPlansDto.getSubscriptionPlanId())))) {
					subscriptionPlans = subscriptionPlanRepository.findBySubscriptionPlanIdAndActive(
							subscriptionPlansDto.getSubscriptionPlanId(), Boolean.TRUE);
					uniqueNameCheck(Boolean.TRUE, subscriptionPlansDto);
					subscriptionPlansOld = new SubscriptionPlans();
					BeanUtils.copyProperties(subscriptionPlans, subscriptionPlansOld);

					BeanUtils.copyProperties(subscriptionPlansDto, subscriptionPlans);
					subscriptionPlans.setModifiedBy(userId);
					subscriptionPlans.setModifiedOn(new Date());
					subscriptionPlans.setActive(Boolean.TRUE);
					subscriptionPlans = subscriptionPlanRepository.save(subscriptionPlans);

					historyFeedId = HistoryFeedUtil.createSaveHistoryFeedObject(systemServiceFeignProxy,
							DataOperation.EDIT.getOperationType(), companyId,
							MasterType.SubscriptionPlan.getMasterName(), subscriptionPlans.getSubscriptionPlanId(),
							subscriptionPlans.getPlanName(), DataOperation.MANUAL_ENTRY.getOperationType(), headers);
					svmUtil.saveChangedField(
							ObjectUtils.getChangedFields(subscriptionPlansOld, subscriptionPlans), historyFeedId);

					log.info("Updated subscription plan with id: {}", subscriptionPlans.getSubscriptionPlanId());
				} else {
					uniqueNameCheck(Boolean.FALSE, subscriptionPlansDto);
					subscriptionPlans = new SubscriptionPlans();
					BeanUtils.copyProperties(subscriptionPlansDto, subscriptionPlans);
					subscriptionPlans.setCreatedBy(userId);
					subscriptionPlans.setCreatedOn(new Date());
					subscriptionPlans.setActive(Boolean.TRUE);
					subscriptionPlans = subscriptionPlanRepository.save(subscriptionPlans);

					  HistoryFeedUtil.createSaveHistoryFeedObject(systemServiceFeignProxy,
							DataOperation.ADD.getOperationType(), companyId,
							MasterType.SubscriptionPlan.getMasterName(), subscriptionPlans.getSubscriptionPlanId(),
							subscriptionPlans.getPlanName(), DataOperation.MANUAL_ENTRY.getOperationType(), headers);
					log.info("Added new subscription plan with id: {}", subscriptionPlans.getSubscriptionPlanId());
				}
				if (subscriptionPlans != null) {
					String planName = subscriptionPlans.getPlanName();
					List<CompanyDetails> companyDetailsList = companyDetailsRepository
							.findBySubIdAndActive(subscriptionPlans.getSubscriptionPlanId());
					if (!companyDetailsList.isEmpty()) {
						companyDetailsList.forEach(companyDetails -> {
							companyDetails.setModifiedOn(new Date());
							companyDetails.setPlanName(planName);
						});
						companyDetailsRepository.saveAll(companyDetailsList);
					}
				}
			}
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		response.put("subscriptionPlanId", subscriptionPlans.getSubscriptionPlanId());
		response.put(Constants.SUCCESS, success);
		response.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	public void uniqueNameCheck(Boolean isEdit, SubscriptionPlansDto subscriptionPlansDto)
			throws ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		SubscriptionPlans subscriptionPlans = subscriptionPlanRepository
				.findOneByPlanName(subscriptionPlansDto.getPlanName());
		if (Boolean.TRUE.equals(isEdit)) {
			if (subscriptionPlans != null && !subscriptionPlans.getSubscriptionPlanId()
					.equals(subscriptionPlansDto.getSubscriptionPlanId())) {
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
						"Subscription Plan Name : " + subscriptionPlansDto.getPlanName() + ", Already Exist");
			}
		} else {
			if (subscriptionPlans != null) {
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
						"Subscription Plan Name : " + subscriptionPlansDto.getPlanName() + ", Already Exist");
			}
		}
		log.info(LogUtil.exitLog(CLASSNAME));
	}

	@Override
	public Map<String, Object> getSubscriptionPlansById(Long subscriptionPlanId, MultiValueMap<String, String> headers)
			throws ContractException, BussinessException, TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));
		response = new HashMap<>();
		Map<String, Object> userResponse = userUtil.tokenVerification(headers);
		String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));
		SuccessResponse success = new SuccessResponse();
		SubscriptionPlans subscriptionPlans = null;
		SubscriptionPlansDto subscriptionPlansDto = new SubscriptionPlansDto();

		try {
			if (Boolean.TRUE.equals(CompanyManagmentValidatorUtil.idValidator(subscriptionPlanId, language))) {
				subscriptionPlans = subscriptionPlanRepository.findBySubscriptionPlanId(subscriptionPlanId);
				log.info("subscriptionPlans  --------------------> " + subscriptionPlans.toString());
				if (subscriptionPlans != null) {
					BeanUtils.copyProperties(subscriptionPlans, subscriptionPlansDto);
					if (subscriptionPlans.getCreatedOn() != null) {
						subscriptionPlansDto.setCreatedOn(
								DateFormatUtils.format(subscriptionPlans.getCreatedOn(), DateUtil.dd_MM_yyyy));
					}
					if (subscriptionPlans.getModifiedOn() != null) {
						subscriptionPlansDto.setModifiedOn(
								DateFormatUtils.format(subscriptionPlans.getModifiedOn(), DateUtil.dd_MM_yyyy));
					}
					success.setMessage("subscriptionPlan found");
				}
			}
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		response.put("subscriptionPlans", subscriptionPlansDto);
		response.put(Constants.SUCCESS, success);
		response.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	@Override
	public Map<String, Object> deleteSubscriptionPlansById(Long subscriptionPlanId, boolean status,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException {
		response = new HashMap<>();
		Map<String, Object> userResponse = userUtil.tokenVerification(headers);
		String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));
		Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));
		SuccessResponse success = new SuccessResponse();
		log.info(LogUtil.startLog(CLASSNAME));
		try {
			if (Boolean.TRUE.equals(CompanyManagmentValidatorUtil.idValidator(subscriptionPlanId, language))) {
				SubscriptionPlans subscriptionPlans = subscriptionPlanRepository
						.findBySubscriptionPlanIdAndActive(subscriptionPlanId, Boolean.TRUE);
				if (subscriptionPlans != null) {
					subscriptionPlans.setActive(status);
					subscriptionPlans = subscriptionPlanRepository.save(subscriptionPlans);

					HistoryFeedUtil.createSaveHistoryFeedObject(systemServiceFeignProxy,
							HistoryFeedOperation.saveActiveDeleteOperation(status), companyId,
							MasterType.SubscriptionPlan.getMasterName(), subscriptionPlans.getSubscriptionPlanId(),
							subscriptionPlans.getPlanName(), DataOperation.MANUAL_ENTRY.getOperationType(), headers);
					success.setMessage(Constants.SUCCESS);
				}
			}
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		response.put(Constants.SUCCESS, success);
		response.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	@Override
	public Map<String, Object> findAllSubscriptionPlansPaginationCriteria1(List<SequenceColumnDTO> sequenceColumnDTOs,
			int draw, int start, int length, String columns, String search, String sortOrder, String sortField,
			String searchCol, boolean booleanfield, MultiValueMap<String, String> headers)
			throws ContractException, BussinessException, TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));
		if (length == 0)
			length = Constants.ONE_BILLION;
		Map<String, Object> map = new HashMap<>();
		SuccessResponse success = new SuccessResponse();
		List<SubscriptionPlansDto> subscriptionPlansDtos = new ArrayList<>();
		List<SubscriptionPlans> subscriptionPlans = null;
		List<ColumnDetailsDTO> columnDetailsDTOs = new ArrayList<>();
		Long count = 0L;
		
		try {

			userUtil.tokenVerification(headers);
			
			PageRequest pageRequest = PageRequest.of(start, length,
					org.springframework.data.domain.Sort.Direction.fromString(sortOrder), sortField);

			Page<SubscriptionPlans> page = paginateSubscriptionPlans(search, searchCol, pageRequest, booleanfield);
			if (page != null) {
				subscriptionPlans = page.getContent();
				count = page.getTotalElements();
			}
			if (subscriptionPlans != null) {
				convertSubscriptionPlansToDtoWithDetails(success, subscriptionPlansDtos, subscriptionPlans);
			}
			if (CollectionUtils.isNotEmpty(sequenceColumnDTOs)) {
				for (SequenceColumnDTO sequenceColumnDTO : sequenceColumnDTOs) {
					if (ObjectUtils.isPositiveNonZero(sequenceColumnDTO.getColumnID())
							&& ObjectUtils.isPositiveNonZero(sequenceColumnDTO.getSequenceColumn())) {
						ColumnDetailsDTO columnDetailsDTO = columnUtil
								.findByColumnId(sequenceColumnDTO.getColumnID());
						if (columnDetailsDTO != null) {
							columnDetailsDTOs.add(columnDetailsDTO);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		map.put(Constants.COUNT, count);
		map.put(Constants.COLUMN, columnDetailsDTOs);
		map.put("subscriptionPlans", subscriptionPlansDtos);
		map.put(Constants.SUCCESS, success);
		map.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	private void convertSubscriptionPlansToDtoWithDetails(SuccessResponse success, List<SubscriptionPlansDto> subscriptionPlansDtos,
			List<SubscriptionPlans> subscriptionPlans) {
		SubscriptionPlansDto subscriptionPlansDto;
		for (SubscriptionPlans plans : subscriptionPlans) {
			subscriptionPlansDto = new SubscriptionPlansDto();
			BeanUtils.copyProperties(plans, subscriptionPlansDto);
			subscriptionPlansDto.setTypeName(
					plans.getType() != null ? SubscriptionType.getTypeName(plans.getType()) : null);
			if (ObjectUtils.isPositiveNonZero(plans.getSubscriptionPlanId())) {
				List<Long> numOfCompanys = companyDetailsRepository.findBySubId(plans.getSubscriptionPlanId());
				if (CollectionUtils.isNotEmpty(numOfCompanys)) {
					subscriptionPlansDto.setNumOfCompanys(Long.valueOf(String.valueOf(numOfCompanys.size())));
				}
			}
			subscriptionPlansDtos.add(subscriptionPlansDto);
		}
		success.setMessage("subscriptionPlans found");
	}

	private Page<SubscriptionPlans> paginateSubscriptionPlans(String search, String searchCol, PageRequest pageRequest,
			boolean booleanfield) throws TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SubscriptionPlans> criteria = builder.createQuery(SubscriptionPlans.class);
		Root<SubscriptionPlans> root = criteria.from(SubscriptionPlans.class);
		List<Predicate> predicates = new ArrayList<>();
		Predicate activepredicate = null;
		activepredicate = builder.equal(root.get(Constants.ACTIVE), booleanfield);
		if (StringUtils.isNotEmpty(searchCol)) {
			if (searchCol.equalsIgnoreCase(Constants.TYPE)) {
				if ("whitelabel".contains(search.toLowerCase())) {
					predicates.add(builder.equal(root.get(Constants.TYPE), 0L));
				} else if ("cobranding".contains(search.toLowerCase())) {
					predicates.add(builder.equal(root.get(Constants.TYPE), 1L));
				} else if ("nobranding".contains(search.toLowerCase())) {
					predicates.add(builder.equal(root.get(Constants.TYPE), 2L));
				}
			} else {

				predicates.add(builder.like(builder.lower(root.get(searchCol)),
						Constants.LIKE_STRING.replace(Constants.LIKE, search.toLowerCase())));
			}
		} else {
			predicates.add(builder.like(builder.lower(root.get("planName")),
					Constants.LIKE_STRING.replace(Constants.LIKE, search.toLowerCase())));
			predicates.add(builder.like(builder.lower(root.get("planDescription")),
					Constants.LIKE_STRING.replace(Constants.LIKE, search.toLowerCase())));
		}
		Predicate searchpredicatevar = builder.or(predicates.toArray(new Predicate[predicates.size()]));
		criteria.where(builder.and(searchpredicatevar, activepredicate));
		criteria.orderBy(QueryUtils.toOrders(pageRequest.getSort(), root, builder));
		List<SubscriptionPlans> result = em.createQuery(criteria).setFirstResult((int) pageRequest.getOffset())
				.setMaxResults(pageRequest.getPageSize()).getResultList();
		List<SubscriptionPlans> result1 = em.createQuery(criteria).getResultList();
		Integer count = result1.size();
		log.info(LogUtil.exitLog(CLASSNAME));
		return new PageImpl<>(result, pageRequest, count);
	}

	@Override
	public Map<String, Object> subscriptionPlanUserLimit(MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		SuccessResponse success = null;

		try {
			Map<String, Object> userResponse = userUtil.tokenVerification(headers);
			Long userCompanyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

			Map<String, Object> companyMap = companyDetailsService.findCompanyDetailsById(userCompanyId, headers);

			if (companyMap.containsKey(COMPANY_DETAILS)) {
				CompanyDetailsDTO dto = (CompanyDetailsDTO) companyMap.get(COMPANY_DETAILS);
				if (dto != null && ObjectUtils.isPositiveNonZero(dto.getSubId())) {
					SubscriptionPlans subplans = subscriptionPlanRepository
							.findBySubscriptionPlanIdAndActive(dto.getSubId(), Boolean.TRUE);
					Integer number = userUtil.getUserCountByCompanyId(headers, userCompanyId);
					if (number < subplans.getNumOfUsers()) {
						success = new SuccessResponse();
						success.setMessage(EnglishConstants.SP0008);
					} else {
						log.error(EnglishConstants.SP0009);
						throw new BussinessException(HttpStatus.EXPECTATION_FAILED, EnglishConstants.SP0009);
					}
				}
			}
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}

		map.put(Constants.SUCCESS, success);
		map.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	@Override
	public Map<String, Object> getSubscriptionPlanByCompanyId(MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		SuccessResponse success = null;
		
		try {
			Map<String, Object> userResponse = userUtil.tokenVerification(headers);
			Long userCompanyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));
			log.info("User company ID retrieved: {}", userCompanyId);

			Map<String, Object> companyMap = companyDetailsService.findCompanyDetailsById(userCompanyId, headers);
			if (companyMap.containsKey(COMPANY_DETAILS)) {
				CompanyDetailsDTO dto = (CompanyDetailsDTO) companyMap.get(COMPANY_DETAILS);
				if (dto != null && ObjectUtils.isPositiveNonZero(dto.getSubId())) {
					log.info("Subscription ID found for company: {}", dto.getSubId());
					SubscriptionPlans subplans = subscriptionPlanRepository
							.findBySubscriptionPlanIdAndActive(dto.getSubId(), Boolean.TRUE);
					SubscriptionPlansDto subscriptionPlansDto = new SubscriptionPlansDto();
					BeanUtils.copyProperties(subplans, subscriptionPlansDto);
					map.put(Constants.TYPE,
							subscriptionPlansDto.getType() != null ? subscriptionPlansDto.getType() : null);
					map.put("subscriptionPlansDto", subscriptionPlansDto);
					success = new SuccessResponse();
					success.setMessage(Constants.SUCCESS);
					log.info("Subscription plan details retrieved successfully.");
				} else {
					log.info("No valid subscription ID found for company.");
				}
			} else {
				log.info("Company details not found for company ID: {}", userCompanyId);
			}

		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException | NumberFormatException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		map.put(Constants.SUCCESS, success);
		map.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

}
