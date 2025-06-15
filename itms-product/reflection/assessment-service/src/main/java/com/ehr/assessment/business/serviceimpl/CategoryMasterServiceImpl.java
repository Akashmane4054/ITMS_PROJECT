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

import com.ehr.assessment.business.dto.CategoryMasterDto;
import com.ehr.assessment.business.service.CategoryMasterService;
import com.ehr.assessment.business.validation.CategoryMasterValidator;
import com.ehr.assessment.business.validation.ConstantsUtil;
import com.ehr.assessment.business.validation.EnglishConstants;
import com.ehr.assessment.integration.domain.CategoryMaster;
import com.ehr.assessment.integration.repository.CategoryRepository;
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
public class CategoryMasterServiceImpl implements CategoryMasterService {

	private static final String CLASSNAME = CategoryMasterServiceImpl.class.getSimpleName();

	private static final String CATEGORY_MASTER_NAME = "name";
	SimpleDateFormat dynamicDateFormat = new SimpleDateFormat(DateUtil.DATE_FORMAT4);

	Map<String, Object> response = null;

	@PersistenceContext
	EntityManager em;

	private final UserUtil userUtil;
	private final ColumnServiceFeignProxy columnServiceFeignProxy;
	private final SystemServiceFeignProxy systemServiceFeignProxy;
	private final CategoryRepository categoryRepository;

	@Override
	public Map<String, Object> saveCategoryMaster(CategoryMasterDto categoryMasterDto,
			MultiValueMap<String, String> headers) throws ContractException, TechnicalException, BussinessException {

		log.info(LogUtil.startLog(CLASSNAME));
		response = new HashMap<>();
		Map<String, Object> userResponse = getUserDetails(headers);
		try {

			Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
			String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));
			Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

			CategoryMasterValidator.dtoValidationUtil(categoryMasterDto, language);

			CategoryMaster existingCategoryMaster = categoryRepository.findByIdAndActive(categoryMasterDto.getId(),
					Boolean.TRUE);

			if (existingCategoryMaster != null) {
				updateCategoryMaster(existingCategoryMaster, categoryMasterDto, userId, companyId, headers);
			} else {
				createNewCategoryMaster(categoryMasterDto, userId, companyId, headers);
			}
			response.put(Constants.SUCCESS, new SuccessResponse("Category Master Saved"));

		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	private Map<String, Object> getUserDetails(MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException {
		return ExceptionUtil.throwExceptionsIfPresent(userUtil.tokenVerification(headers));
	}

	private void updateCategoryMaster(CategoryMaster existingCategoryMaster, CategoryMasterDto categoryMasterDto,
			Long userId, Long companyId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		CategoryMaster oldCategoryMaster = new CategoryMaster();
		BeanUtils.copyProperties(existingCategoryMaster, oldCategoryMaster);

		uniqueNameCheck(Boolean.TRUE, categoryMasterDto);

		existingCategoryMaster.setName(categoryMasterDto.getName().trim());
		existingCategoryMaster.setDescription(StringUtils.isEmpty(categoryMasterDto.getDescription()) ? ""
				: categoryMasterDto.getDescription().trim());
		existingCategoryMaster.setModifiedOn(new Date());
		existingCategoryMaster.setModifiedBy(userId);
		existingCategoryMaster.setActive(Boolean.TRUE);
		categoryRepository.save(existingCategoryMaster);

		Long historyFeedId = HistoryFeedUtil.createSaveHistoryFeedObject(systemServiceFeignProxy,
				DataOperation.EDIT.getOperationType(), companyId, MasterType.CategoryMaster.getMasterName(),
				existingCategoryMaster.getId(), existingCategoryMaster.getName(),
				DataOperation.MANUAL_ENTRY.getOperationType(), headers);

		systemServiceFeignProxy.saveChangedField(
				ObjectUtils.getChangedFields(oldCategoryMaster, existingCategoryMaster), historyFeedId);

	}

	private void createNewCategoryMaster(CategoryMasterDto categoryMasterDto, Long userId, Long companyId,
			MultiValueMap<String, String> headers) throws BussinessException, TechnicalException, ContractException {
		// Perform unique name validation
		uniqueNameCheck(Boolean.FALSE, categoryMasterDto);

		// Create a new category
		CategoryMaster newCategoryMaster = new CategoryMaster();
		BeanUtils.copyProperties(categoryMasterDto, newCategoryMaster);
		newCategoryMaster.setName(categoryMasterDto.getName().trim());
		newCategoryMaster.setDescription(StringUtils.isEmpty(categoryMasterDto.getDescription()) ? ""
				: categoryMasterDto.getDescription().trim());
		newCategoryMaster.setCreatedOn(new Date());
		newCategoryMaster.setCreatedBy(userId);
		newCategoryMaster.setActive(Boolean.TRUE);
		// Save the new category
		categoryRepository.save(newCategoryMaster);

		// Create history feed
		HistoryFeedUtil.createSaveHistoryFeedObject(systemServiceFeignProxy, DataOperation.ADD.getOperationType(),
				companyId, MasterType.CategoryMaster.getMasterName(), newCategoryMaster.getId(),
				newCategoryMaster.getName(), DataOperation.MANUAL_ENTRY.getOperationType(), headers);
	}

	@Override
	public Map<String, Object> findCategoryMasterById(Long id, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		CategoryMasterDto dto;
		response = new HashMap<>();
		Map<String, Object> userResponse = getUserDetails(headers);
		try {
			String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));

			CategoryMasterValidator.isPrimaryIdPresent(id, language);

			CategoryMaster categoryMaster = categoryRepository.findByIdAndActive(id, Boolean.TRUE);
			if (categoryMaster == null) {
				log.error(EnglishConstants.CM0002);
				throw new BussinessException(HttpStatus.NOT_FOUND, ConstantsUtil.getConstant("CM0002", language));
			}
			dto = new CategoryMasterDto();
			BeanUtils.copyProperties(categoryMaster, dto);
			dto.setCreatedOn(dynamicDateFormat.format(categoryMaster.getCreatedOn()));
			// time zone
			if (categoryMaster.getModifiedOn() != null) {
				dto.setModifiedOn(dynamicDateFormat.format(categoryMaster.getModifiedOn()));
			}

			response.put("categoryMaster", dto);
			response.put(Constants.SUCCESS, new SuccessResponse("Category Master found"));
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
	public Map<String, Object> deleteCategoryMaster(Long id, boolean active, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		response = new HashMap<>();
		Optional<CategoryMaster> categoryMaster;
		Map<String, Object> userResponse = getUserDetails(headers);
		try {
			String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));

			CategoryMasterValidator.isPrimaryIdPresent(id, language);

			categoryMaster = categoryRepository.findById(id);
			if (categoryMaster.isEmpty()) {
				log.error(EnglishConstants.CM0002);
				throw new BussinessException(HttpStatus.NOT_ACCEPTABLE, ConstantsUtil.getConstant("CM0002", language));
			}
			categoryMaster.get().setActive(active);
			categoryMaster = Optional.of(categoryRepository.save(categoryMaster.get()));
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
	public Map<String, Object> findAllCategoryMasterPaginationCriteria(List<SequenceColumnDTO> sequenceColumnDTOs,
			String draw, int start, int length, String columns, String search, String sortOrder, String sortField,
			String searchCol, boolean booleanfield, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		if (length == 0)
			length = Constants.ONE_BILLION;
		Map<String, Object> map = new HashMap<>();
		SuccessResponse success = new SuccessResponse();
		List<CategoryMasterDto> categoryMasterDtoList = new ArrayList<>();
		List<CategoryMaster> categoryMasterList = null;
		CategoryMasterDto categoryMasterDto = null;
		List<ColumnDetailsDTO> columnDetailsDTOs = new ArrayList<>();
		Long count = 0L;
		getUserDetails(headers);
		try {
			PageRequest pageRequest = PageRequest.of(start, length,
					org.springframework.data.domain.Sort.Direction.fromString(sortOrder), sortField);
			Page<CategoryMaster> page = paginateCategoryMaster(search, searchCol, pageRequest, booleanfield);
			if (page != null) {
				categoryMasterList = page.getContent();
				count = page.getTotalElements();
			}
			if (categoryMasterList != null) {
				for (CategoryMaster plan : categoryMasterList) {
					categoryMasterDto = new CategoryMasterDto();
					BeanUtils.copyProperties(plan, categoryMasterDto);
					categoryMasterDtoList.add(categoryMasterDto);
				}
				success.setMessage("categoryMasterList found");
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
		map.put("categoryMasterList", categoryMasterDtoList);
		map.put(Constants.SUCCESS, success);
		map.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	@Override
	public Map<String, Object> getAllActiveCategory(MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		CategoryMasterDto categoryMasterDto = null;
		List<CategoryMaster> categoryMasterList = null;
		List<CategoryMasterDto> categoryMasterDtoList = new ArrayList<>();
		getUserDetails(headers);
		try {
			categoryMasterList = categoryRepository.findByActive(true);
			if (CollectionUtils.isNotEmpty(categoryMasterList)) {
				for (CategoryMaster categoryMaster : categoryMasterList) {
					categoryMasterDto = new CategoryMasterDto();
					BeanUtils.copyProperties(categoryMaster, categoryMasterDto);
					categoryMasterDto.setCreatedOn(dynamicDateFormat.format(categoryMaster.getCreatedOn()));
					if (categoryMaster.getModifiedOn() != null) {
						categoryMasterDto.setModifiedOn(dynamicDateFormat.format(categoryMaster.getModifiedOn()));
					}
					categoryMasterDtoList.add(categoryMasterDto);
				}
				map.put("categoryMasterDtoList", categoryMasterDtoList);
			} else {
				map.put("categoryMasterDtoList", Collections.emptyList());
			}
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {

			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(this.getClass().getName()));
		return map;
	}

	private Page<CategoryMaster> paginateCategoryMaster(String search, String searchcolumn, Pageable pageable,
			boolean booleanfield) throws TechnicalException, BussinessException, ContractException, ParseException {
		log.info(LogUtil.startLog(this.getClass().getName()));

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CategoryMaster> criteria = builder.createQuery(CategoryMaster.class);
		Root<CategoryMaster> root = criteria.from(CategoryMaster.class);
		List<Predicate> predicates = new ArrayList<>();
		Predicate activepredicate = builder.equal(root.get(Constants.ACTIVE), booleanfield);

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
					|| searchcolumn.equals("id")) {
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
			predicates.add(builder.like(builder.lower(root.get(CATEGORY_MASTER_NAME)),
					Constants.LIKE_STRING.replace(Constants.LIKE, search.toLowerCase())));
		}
		Predicate searchpredicatevar = builder.and(predicates.toArray(new Predicate[predicates.size()]));
		criteria.where(builder.and(searchpredicatevar, activepredicate));
		criteria.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));
		List<CategoryMaster> result = em.createQuery(criteria).setFirstResult((int) pageable.getOffset())
				.setMaxResults(pageable.getPageSize()).getResultList();
		List<CategoryMaster> result1 = em.createQuery(criteria).getResultList();
		int count = result1.size();
		log.info(LogUtil.startLog(CLASSNAME));
		return new PageImpl<>(result, pageable, count);
	}

	public void uniqueNameCheck(Boolean isEdit, CategoryMasterDto categoryMasterDto) throws ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		CategoryMaster categoryMaster = categoryRepository.findByName(categoryMasterDto.getName());
		if (Boolean.TRUE.equals(isEdit)) {
			if (categoryMaster != null && !categoryMaster.getId().equals(categoryMaster.getId())) {
				log.error(EnglishConstants.CM0001);
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
						"Category Master Name : " + categoryMasterDto.getName() + ", Already Exist");
			}
		} else {
			if (categoryMaster != null) {
				log.error(EnglishConstants.CM0001);
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
						"Category Master Name : " + categoryMasterDto.getName() + ", Already Exist");
			}
		}
		log.info(LogUtil.startLog(CLASSNAME));
	}
}
