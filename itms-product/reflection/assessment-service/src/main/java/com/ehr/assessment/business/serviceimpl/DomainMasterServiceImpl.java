package com.ehr.assessment.business.serviceimpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.ehr.assessment.business.dto.DomainMasterDto;
import com.ehr.assessment.business.service.DomainMasterService;
import com.ehr.assessment.business.validation.ConstantsUtil;
import com.ehr.assessment.business.validation.DomainMasterValidator;
import com.ehr.assessment.business.validation.EnglishConstants;
import com.ehr.assessment.integration.domain.DomainMaster;
import com.ehr.assessment.integration.repository.DomainRepository;
import com.ehr.core.dto.ColumnDetailsDTO;
import com.ehr.core.dto.SequenceColumnDTO;
import com.ehr.core.dto.SuccessResponse;
import com.ehr.core.enums.DataOperation;
import com.ehr.core.enums.MasterType;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.feignclients.ColumnServiceFeignProxy;
import com.ehr.core.feignclients.SystemServiceFeignProxy;
import com.ehr.core.util.Constants;
import com.ehr.core.util.DateUtil;
import com.ehr.core.util.ExceptionUtil;
import com.ehr.core.util.HistoryFeedUtil;
import com.ehr.core.util.LogUtil;
import com.ehr.core.util.ObjectUtils;
import com.ehr.core.util.UserUtil;

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
public class DomainMasterServiceImpl implements DomainMasterService {

	private static final String CLASSNAME = DomainMasterServiceImpl.class.getSimpleName();

	private static final String DOMAIN_MASTER_NAME = "domainName";
	private static final String DOMAIN_MASTER_ID = "domainId";
	SimpleDateFormat dynamicDateFormat = new SimpleDateFormat(DateUtil.DATE_FORMAT4);

	Map<String, Object> response = null;

	@PersistenceContext
	EntityManager em;

	private final UserUtil userUtil;
	private final ColumnServiceFeignProxy columnServiceFeignProxy;
	private final SystemServiceFeignProxy systemServiceFeignProxy;
	private final DomainRepository domainRepository;

	@Override
	public Map<String, Object> saveDomainMaster(DomainMasterDto domainMasterDto, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException {

		log.info(LogUtil.startLog(CLASSNAME));
		response = new HashMap<>();
		Map<String, Object> userResponse = getUserDetails(headers);

		try {
			Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
			String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));
			Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

			DomainMasterValidator.dtoValidationUtil(domainMasterDto, language);

			DomainMaster existingDomainMaster = domainRepository.findByDomainIdAndActive(domainMasterDto.getDomainId(),
					Boolean.TRUE);

			if (existingDomainMaster != null) {
				updateDomainMaster(existingDomainMaster, domainMasterDto, userId, companyId, headers);
			} else {
				createNewDomainMaster(domainMasterDto, userId, companyId, headers);
			}
			response.put(Constants.SUCCESS, new SuccessResponse("Domain Master Saved"));

		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	@Override
	public Map<String, Object> findDomainMasterById(Long id, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		DomainMasterDto dto;
		response = new HashMap<>();
		Map<String, Object> userResponse = getUserDetails(headers);
		try {
			String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));

			DomainMasterValidator.isPrimaryIdPresent(id, language);

			DomainMaster domainMaster = domainRepository.findByDomainIdAndActive(id, Boolean.TRUE);
			if (domainMaster == null) {
				log.error(EnglishConstants.DM0002);
				throw new BussinessException(HttpStatus.NOT_FOUND, ConstantsUtil.getConstant("DM0002", language));
			}
			dto = new DomainMasterDto();
			BeanUtils.copyProperties(domainMaster, dto);
			dto.setCreatedOn(dynamicDateFormat.format(domainMaster.getCreatedOn()));
			// time zone
			if (domainMaster.getModifiedOn() != null) {
				dto.setModifiedOn(dynamicDateFormat.format(domainMaster.getModifiedOn()));
			}

			response.put("domainMaster", dto);
			response.put(Constants.SUCCESS, new SuccessResponse("Domain Master found"));
			response.put(Constants.ERROR, null);
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.startLog(CLASSNAME));
		return response;
	}

	@Override
	public Map<String, Object> deleteDomainMaster(Long id, boolean active, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		response = new HashMap<>();
		Optional<DomainMaster> domainMaster;
		Map<String, Object> userResponse = getUserDetails(headers);
		try {
			String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));

			DomainMasterValidator.isPrimaryIdPresent(id, language);

			domainMaster = domainRepository.findByDomainId(id);
			if (domainMaster.isEmpty()) {
				log.error(EnglishConstants.DM0002);
				throw new BussinessException(HttpStatus.NOT_ACCEPTABLE, ConstantsUtil.getConstant("DM0002", language));
			}
			domainMaster.get().setActive(active);
			domainMaster = Optional.of(domainRepository.save(domainMaster.get()));
			response.put(Constants.SUCCESS, new SuccessResponse(Constants.SUCCESS));
			response.put(Constants.ERROR, null);
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.startLog(CLASSNAME));
		return response;
	}

	@Override
	public Map<String, Object> findAllDomainMasterPaginationCriteria(List<SequenceColumnDTO> sequenceColumnDTOs,
			String drawStr, int start, int length, String columns, String search, String sortOrder, String sortField,
			String searchCol, boolean status, MultiValueMap<String, String> headers)
			throws TechnicalException, ContractException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		if (length == 0)
			length = Constants.ONE_BILLION;
		Map<String, Object> map = new HashMap<>();
		SuccessResponse success = new SuccessResponse();
		List<DomainMasterDto> domainMasterDtoList = new ArrayList<>();
		List<DomainMaster> domainMasterList = null;
		DomainMasterDto domainMasterDto = null;
		List<ColumnDetailsDTO> columnDetailsDTOs = new ArrayList<>();
		Long count = 0L;
		getUserDetails(headers);
		try {
			PageRequest pageRequest = PageRequest.of(start, length,
					org.springframework.data.domain.Sort.Direction.fromString(sortOrder), sortField);
			Page<DomainMaster> page = paginateDomainMaster(search, searchCol, pageRequest, status);
			if (page != null) {
				domainMasterList = page.getContent();
				count = page.getTotalElements();
			}
			if (domainMasterList != null) {
				for (DomainMaster plan : domainMasterList) {
					domainMasterDto = new DomainMasterDto();
					BeanUtils.copyProperties(plan, domainMasterDto);
					domainMasterDtoList.add(domainMasterDto);
				}
				success.setMessage("domainMasterList found");
			}
			if (CollectionUtils.isNotEmpty(sequenceColumnDTOs)) {
				for (SequenceColumnDTO sequenceColumnDTO : sequenceColumnDTOs) {
					if (ObjectUtils.isPositiveNonZero(sequenceColumnDTO.getColumnID())
							&& ObjectUtils.isPositiveNonZero(sequenceColumnDTO.getSequenceColumn())) {
						ColumnDetailsDTO columnDetailsDTO = columnServiceFeignProxy
								.findBycolumnId(sequenceColumnDTO.getColumnID());
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
		map.put("domainMasterList", domainMasterDtoList);
		map.put(Constants.SUCCESS, success);
		map.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	@Override
	public Map<String, Object> getAllActivedDomain(MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		DomainMasterDto domainMasterDto = null;
		List<DomainMaster> domainMasterList = null;
		List<DomainMasterDto> domainMasterDtoList = new ArrayList<>();
		getUserDetails(headers);
		try {
			domainMasterList = domainRepository.findByActive(true);
			if (CollectionUtils.isNotEmpty(domainMasterList)) {
				for (DomainMaster domainMaster : domainMasterList) {
					domainMasterDto = new DomainMasterDto();
					BeanUtils.copyProperties(domainMaster, domainMasterDto);
					domainMasterDto.setCreatedOn(dynamicDateFormat.format(domainMaster.getCreatedOn()));
					if (domainMaster.getModifiedOn() != null) {
						domainMasterDto.setModifiedOn(dynamicDateFormat.format(domainMaster.getModifiedOn()));
					}
					domainMasterDtoList.add(domainMasterDto);
				}
				map.put("domainMasterDtoList", domainMasterDtoList);
			} else {
				map.put("domainMasterDtoList", Collections.emptyList());
			}
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {

			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(this.getClass().getName()));
		return map;
	}

	private Page<DomainMaster> paginateDomainMaster(String search, String searchcolumn, Pageable pageable,
			boolean status) throws TechnicalException, BussinessException, ContractException, ParseException {
		log.info(LogUtil.startLog(this.getClass().getName()));

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<DomainMaster> criteria = builder.createQuery(DomainMaster.class);
		Root<DomainMaster> root = criteria.from(DomainMaster.class);
		List<Predicate> predicates = new ArrayList<>();
		Predicate activepredicate = builder.equal(root.get(Constants.ACTIVE), status);

		if ((searchcolumn != null) && (!searchcolumn.isEmpty())) {
			if (searchcolumn.equals("createdOn") || searchcolumn.equals("modifiedOn")) {
				try {
					LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.valueOf(search)),
							ZoneId.systemDefault());
					ZonedDateTime startOfDay = localDateTime.atZone(ZoneId.systemDefault()).withHour(0).withMinute(0)
							.withSecond(0).withNano(0);
					ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
					Instant startInstant = startOfDay.toInstant();
					Instant endInstant = endOfDay.toInstant();

					Date startDate = Date.from(startInstant);
					Date endDate = Date.from(endInstant);
					Expression<Date> startDateExpr = builder.literal(startDate);
					Expression<Date> endDateExpr = builder.literal(endDate);

					predicates.add(builder.between(root.get(searchcolumn).as(java.util.Date.class), startDateExpr,
							endDateExpr));
				} catch (NumberFormatException e) {
					log.error("Invalid timestamp format for " + searchcolumn + ": " + search, e);
				}
			} else if (searchcolumn.equals("createdBy") || searchcolumn.equals("modifiedBy")
					|| searchcolumn.equals(DOMAIN_MASTER_ID)) {
				try {
					predicates.add(builder.equal(root.get(searchcolumn), Long.parseLong(search)));
				} catch (NumberFormatException e) {
					log.error("numeric search was a string");
				}
			} else {
				predicates.add(builder.like(builder.lower(root.get(searchcolumn)),
						Constants.LIKE_STRING.replace(Constants.LIKE, search.toLowerCase())));

			}
		} else {
			predicates.add(builder.like(builder.lower(root.get(DOMAIN_MASTER_NAME)),
					Constants.LIKE_STRING.replace(Constants.LIKE, search.toLowerCase())));
		}
		Predicate searchpredicatevar = builder.and(predicates.toArray(new Predicate[predicates.size()]));
		criteria.where(builder.and(searchpredicatevar, activepredicate));
		criteria.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));
		List<DomainMaster> result = em.createQuery(criteria).setFirstResult((int) pageable.getOffset())
				.setMaxResults(pageable.getPageSize()).getResultList();
		List<DomainMaster> result1 = em.createQuery(criteria).getResultList();
		int count = result1.size();
		log.info(LogUtil.startLog(CLASSNAME));
		return new PageImpl<>(result, pageable, count);
	}

	private Map<String, Object> getUserDetails(MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException {
		return ExceptionUtil.throwExceptionsIfPresent(userUtil.tokenVerification(headers));
	}

	private void updateDomainMaster(DomainMaster existingDomainMaster, DomainMasterDto domainMasterDto, Long userId,
			Long companyId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		DomainMaster oldDomainMaster = new DomainMaster();
		BeanUtils.copyProperties(existingDomainMaster, oldDomainMaster);

		uniqueNameCheck(Boolean.TRUE, domainMasterDto);

		existingDomainMaster.setDomainName(domainMasterDto.getDomainName().trim());
		existingDomainMaster.setDescription(
				StringUtils.isEmpty(domainMasterDto.getDescription()) ? "" : domainMasterDto.getDescription().trim());
		existingDomainMaster.setModifiedOn(new Date());
		existingDomainMaster.setModifiedBy(userId);
		existingDomainMaster.setActive(Boolean.TRUE);

		domainRepository.save(existingDomainMaster);

		Long historyFeedId = HistoryFeedUtil.createSaveHistoryFeedObject(systemServiceFeignProxy,
				DataOperation.EDIT.getOperationType(), companyId, MasterType.DomainMaster.getMasterName(),
				existingDomainMaster.getDomainId(), existingDomainMaster.getDomainName(),
				DataOperation.MANUAL_ENTRY.getOperationType(), headers);

		systemServiceFeignProxy.saveChangedField(ObjectUtils.getChangedFields(oldDomainMaster, existingDomainMaster),
				historyFeedId);

	}

	private void createNewDomainMaster(DomainMasterDto domainMasterDto, Long userId, Long companyId,
			MultiValueMap<String, String> headers) throws BussinessException, TechnicalException, ContractException {
		// Perform unique name validation
		uniqueNameCheck(Boolean.FALSE, domainMasterDto);

		DomainMaster newDomainMaster = new DomainMaster();
		BeanUtils.copyProperties(domainMasterDto, newDomainMaster);
		newDomainMaster.setDomainName(domainMasterDto.getDomainName().trim());
		newDomainMaster.setDescription(
				StringUtils.isEmpty(domainMasterDto.getDescription()) ? "" : domainMasterDto.getDescription().trim());
		newDomainMaster.setCreatedOn(new Date());
		newDomainMaster.setCreatedBy(userId);
		newDomainMaster.setActive(Boolean.TRUE);

		domainRepository.save(newDomainMaster);

		HistoryFeedUtil.createSaveHistoryFeedObject(systemServiceFeignProxy, DataOperation.ADD.getOperationType(),
				companyId, MasterType.DomainMaster.getMasterName(), newDomainMaster.getDomainId(),
				newDomainMaster.getDomainName(), DataOperation.MANUAL_ENTRY.getOperationType(), headers);
	}

	public void uniqueNameCheck(Boolean isEdit, DomainMasterDto domainMasterDto) throws ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		DomainMaster domainMaster = domainRepository.findByDomainName(domainMasterDto.getDomainName());
		if (Boolean.TRUE.equals(isEdit)) {
			if (domainMaster != null && !domainMaster.getDomainId().equals(domainMaster.getDomainId())) {
				log.error(EnglishConstants.DM0001);
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
						"Domain Master Name : " + domainMasterDto.getDomainName() + ", Already Exist");
			}
		} else {
			if (domainMaster != null) {
				log.error(EnglishConstants.DM0001);
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
						"Domain Master Name : " + domainMasterDto.getDomainName() + ", Already Exist");
			}
		}
		log.info(LogUtil.startLog(CLASSNAME));
	}
}
