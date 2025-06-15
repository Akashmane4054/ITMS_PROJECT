package com.ehr.assessment.business.serviceimpl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
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

import com.ehr.assessment.CustomTransactional;
import com.ehr.assessment.business.dto.CategoryMasterDto;
import com.ehr.assessment.business.dto.DomainMasterDto;
import com.ehr.assessment.business.dto.ElementMasterValueDto;
import com.ehr.assessment.business.dto.ElementOptionsDto;
import com.ehr.assessment.business.dto.ElementsDto;
import com.ehr.assessment.business.dto.SectionDto;
import com.ehr.assessment.business.dto.TemplateDto;
import com.ehr.assessment.business.dto.TemplateRulesDto;
import com.ehr.assessment.business.dto.TemplateRulesThenDto;
import com.ehr.assessment.business.dto.TemplateRulesWhenDto;
import com.ehr.assessment.business.enums.TemplateType;
import com.ehr.assessment.business.service.TemplateService;
import com.ehr.assessment.business.validation.ConstantsUtil;
import com.ehr.assessment.business.validation.EnglishConstants;
import com.ehr.assessment.business.validation.TemplateMasterValidator;
import com.ehr.assessment.integration.domain.CategoryMaster;
import com.ehr.assessment.integration.domain.DomainMaster;
import com.ehr.assessment.integration.domain.ElementMasterValue;
import com.ehr.assessment.integration.domain.ElementOptions;
import com.ehr.assessment.integration.domain.Elements;
import com.ehr.assessment.integration.domain.Section;
import com.ehr.assessment.integration.domain.TemplateCategoryMapping;
import com.ehr.assessment.integration.domain.TemplateDomainMapping;
import com.ehr.assessment.integration.domain.TemplateMaster;
import com.ehr.assessment.integration.domain.TemplateRules;
import com.ehr.assessment.integration.domain.TemplateRulesThen;
import com.ehr.assessment.integration.domain.TemplateRulesWhen;
import com.ehr.assessment.integration.repository.CategoryRepository;
import com.ehr.assessment.integration.repository.DomainRepository;
import com.ehr.assessment.integration.repository.ElementMasterValueRepository;
import com.ehr.assessment.integration.repository.ElementOptionsRepository;
import com.ehr.assessment.integration.repository.ElementsRepository;
import com.ehr.assessment.integration.repository.SectionRepository;
import com.ehr.assessment.integration.repository.TemplateCategoryRepository;
import com.ehr.assessment.integration.repository.TemplateDomainRepository;
import com.ehr.assessment.integration.repository.TemplateRepository;
import com.ehr.assessment.integration.repository.TemplateRulesRepository;
import com.ehr.assessment.integration.repository.TemplateRulesThenRepository;
import com.ehr.assessment.integration.repository.TemplateRulesWhenRepository;
import com.ehr.assessment.presentation.util.Paginate;
import com.ehr.assessment.presentation.util.TemplatePaginate;
import com.ehr.core.dto.ColumnDetailsDTO;
import com.ehr.core.dto.ListingDto;
import com.ehr.core.dto.SequenceColumnDTO;
import com.ehr.core.dto.SortDto;
import com.ehr.core.dto.SuccessResponse;
import com.ehr.core.dto.UserMasterDTO;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.feignclients.ColumnServiceFeignProxy;
import com.ehr.core.feignclients.HealthCompanyServiceFeignProxy;
import com.ehr.core.feignclients.LocationServiceFeignProxy;
import com.ehr.core.util.Constants;
import com.ehr.core.util.DateUtil;
import com.ehr.core.util.ExceptionUtil;
import com.ehr.core.util.LogUtil;
import com.ehr.core.util.ObjectUtils;
import com.ehr.core.util.UserUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

	private static final String TEMPLATE_TYPE = "templateType";

	private static final String CLASSNAME = TemplateServiceImpl.class.getSimpleName();

	private static final String NO_SUCH_TEMPLATE_EXIST = "No such template exist";
	private static final String TEMPLATE_FOUND = "Template found";
	private static final String TEMPLATE_ID = "templateId";
	private static final String TEMPLATE_DTO = "templateDto";
	private static final String USER_DTO = "userDto";
	private static final String SECTION_1 = "Section_1";

	SimpleDateFormat dynamicDateFormat = new SimpleDateFormat(DateUtil.DATE_FORMAT4);
	public static final String DATE_FORMAT4 = "dd-MM-yyyy HH:mm";

	public static final String DATE_FORMAT = "dd-MM-yyyy";

	@PersistenceContext
	EntityManager em;

	private final UserUtil userUtil;
	private final HealthCompanyServiceFeignProxy companyServiceFeignProxy;
	private final LocationServiceFeignProxy locationServiceFeignProxy;
	private final ColumnServiceFeignProxy columnServiceFeignProxy;
	private final SectionRepository sectionRepository;
	private final ElementsRepository elementsRepository;
	private final ElementOptionsRepository elementOptionsRepository;
	private final ElementMasterValueRepository elementMasterValueRepository;
	private final TemplateRepository templateRepository;
	private final TemplateDomainRepository templateDomainMappingRepository;
	private final TemplateCategoryRepository templateCategoryMappingRepository;
	private final DomainRepository domainRepository;
	private final CategoryRepository categoryRepository;
	private final TemplateRulesRepository templateRulesRepository;
	private final TemplateRulesThenRepository templateRulesThenRepository;
	private final TemplateRulesWhenRepository templateRulesWhenRepository;

	@Override
	public Map<String, Object> getTemplate(Long templateId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException {

		log.info(LogUtil.startLog(CLASSNAME));

		Map<String, Object> response = new HashMap<>();

		Map<String, Object> userResponse = verifyToken(headers);

		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
		try {
			TemplateMaster template = findTemplate(templateId);

			TemplateDto templateDto = buildTemplateDto(template, headers, userId);

			response.put(TEMPLATE_DTO, templateDto);
			response.put(Constants.SUCCESS, new SuccessResponse(TEMPLATE_FOUND));
			response.put(Constants.ERROR, null);

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	private Map<String, Object> verifyToken(MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		return ExceptionUtil.throwExceptionsIfPresent(userUtil.tokenVerification(headers));
	}

	private TemplateMaster findTemplate(Long templateId) throws BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		TemplateMaster template = templateRepository.findByIdAndActive(templateId, Boolean.TRUE);
		if (template == null) {
			throw new BussinessException(HttpStatus.NOT_FOUND, NO_SUCH_TEMPLATE_EXIST);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return template;
	}

	private TemplateDto buildTemplateDto(TemplateMaster template, MultiValueMap<String, String> headers, Long userId)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException {

		log.info(LogUtil.startLog(CLASSNAME));
		TemplateDto templateDto = new TemplateDto();
		BeanUtils.copyProperties(template, templateDto);
		templateDto.setTemplateType(template.getTemplateType().getId());
		templateDto.setStatus(template.getStatus());
		templateDto.setCreatedBy(template.getCreatedBy().toString());
		SimpleDateFormat dynamicDateFormat = new SimpleDateFormat(DateUtil.DATE_FORMAT4);
		templateDto.setCreatedOn(dynamicDateFormat.format(template.getCreatedOn()));

		List<TemplateDomainMapping> templateDomainMapping = templateDomainMappingRepository
				.findByTemplateIdAndActive(template.getId(), Boolean.TRUE);
		List<Long> domainIds = templateDomainMapping.stream().map(TemplateDomainMapping::getDomainId)
				.collect(Collectors.toList());

		templateDto.setDomainIds(domainIds);
		List<DomainMasterDto> domainMasterListDto = new ArrayList<>();

		for (TemplateDomainMapping domainMapping : templateDomainMapping) {
			DomainMasterDto domainMasterDto = new DomainMasterDto();
			DomainMaster domainMaster = domainRepository.findByDomainIdAndActive(domainMapping.getDomainId(),
					Boolean.TRUE);
			if (domainMaster != null) {
				BeanUtils.copyProperties(domainMaster, domainMasterDto);
				domainMasterListDto.add(domainMasterDto);
			}
		}
		templateDto.setDomains(domainMasterListDto);

		List<TemplateCategoryMapping> templateCategoryMapping = templateCategoryMappingRepository
				.findByTemplateIdAndActive(template.getId(), Boolean.TRUE);

		List<Long> catIds = templateCategoryMapping.stream().map(TemplateCategoryMapping::getCategoryId)
				.collect(Collectors.toList());
		templateDto.setCategoryIds(catIds);
		List<CategoryMasterDto> categoryMasterListDto = new ArrayList<>();
		for (TemplateCategoryMapping categoryMapping : templateCategoryMapping) {
			CategoryMasterDto categoryMasterDto = new CategoryMasterDto();
			CategoryMaster categoryMaster = categoryRepository.findByIdAndActive(categoryMapping.getCategoryId(),
					Boolean.TRUE);
			if (categoryMaster != null) {
				BeanUtils.copyProperties(categoryMaster, categoryMasterDto);
				categoryMasterListDto.add(categoryMasterDto);
			}
		}
		templateDto.setCategories(categoryMasterListDto);

		List<TemplateRulesDto> templateRulesDto = getTemplateRules(template.getId());
		templateDto.setRules(templateRulesDto);

		List<SectionDto> sectionDto = getSectionDtoForGetTemplate(template.getId(), userId);
		templateDto.setSections(sectionDto);
		if (template.getReviewer() != null) {
			UserMasterDTO reviewerUser = userUtil.getUserById(template.getReviewer(), headers);
			templateDto.setReviewer(reviewerUser.getFullName());
			templateDto.setReviewedOn(formatDate(template.getReviewedOn()));

		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return templateDto;
	}

	private List<SectionDto> getSectionDtoForGetTemplate(Long templateId, Long userId)
			throws IOException, ContractException, TechnicalException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		List<Section> sections = sectionRepository.findByTemplateIdAndActive(templateId, Boolean.TRUE);
		List<SectionDto> sectionListDto = new ArrayList<>();

		for (Section section : sections) {
			SectionDto sectionDto = new SectionDto();
			BeanUtils.copyProperties(section, sectionDto);
			List<Elements> elements = elementsRepository.findBySectionIdAndActive(section.getId(), Boolean.TRUE);
			List<ElementsDto> elementsDto = new ArrayList<>();
			for (Elements element : elements) {

				ElementsDto elementDto = buildElementsDto(element);

				elementsDto.add(elementDto);

			}
			sectionDto.setSectionElements(elementsDto);
			sectionListDto.add(sectionDto);
		}

		log.info(LogUtil.exitLog(CLASSNAME));
		return sectionListDto;
	}

	private ElementsDto buildElementsDto(Elements element) throws TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));
		ElementsDto elementDto = new ElementsDto();
		BeanUtils.copyProperties(element, elementDto);

		List<ElementOptionsDto> masters = fetchElementOptions(element);
		elementDto.setValues(masters);

		List<ElementMasterValueDto> masterValues = fetchElementsValues(element);
		elementDto.setMasterSetValues(masterValues);

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

	private List<ElementMasterValueDto> fetchElementsValues(Elements element) {
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

	@Override
	public List<TemplateRulesDto> getTemplateRules(Long templateId)
			throws BussinessException, ContractException, TechnicalException {
		log.info(LogUtil.startLog(CLASSNAME));

		List<TemplateRulesDto> rulesList = new ArrayList<>();

		try {
			List<TemplateRules> rules = templateRulesRepository.findByTemplateIdAndActive(templateId, Boolean.TRUE);
			for (TemplateRules rule : rules) {
				TemplateRulesDto ruleDto = new TemplateRulesDto();
				BeanUtils.copyProperties(rule, ruleDto);
				List<TemplateRulesThen> thenConditions = templateRulesThenRepository.findByTemplateRuleId(rule.getId());
				List<TemplateRulesWhen> whenConditions = templateRulesWhenRepository.findByTemplateRuleId(rule.getId());
				List<TemplateRulesThenDto> thenConditionsDto = new ArrayList<>();
				for (TemplateRulesThen thenCondition : thenConditions) {
					TemplateRulesThenDto thenConditionDto = new TemplateRulesThenDto();
					BeanUtils.copyProperties(thenCondition, thenConditionDto);
					thenConditionsDto.add(thenConditionDto);
				}
				List<TemplateRulesWhenDto> whenConditionsDto = new ArrayList<>();
				for (TemplateRulesWhen whenCondition : whenConditions) {
					TemplateRulesWhenDto whenConditionDto = new TemplateRulesWhenDto();
					BeanUtils.copyProperties(whenCondition, whenConditionDto);
					whenConditionsDto.add(whenConditionDto);
				}
				ruleDto.setWhen(whenConditionsDto);
				ruleDto.setThen(thenConditionsDto);
				rulesList.add(ruleDto);
			}

		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return rulesList;
	}

	@Override
	@Transactional
	public Map<String, Object> saveTemplateSection(SectionDto sectionDto, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException {

		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> userResponse = verifyToken(headers);
		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
		Map<String, Object> response = new HashMap<>();
		Section section;
		Long sectionId;

		try {
			Section existingSection = sectionRepository.findByNameAndTemplateIdAndActive(sectionDto.getName(),
					sectionDto.getTemplateId(), Boolean.TRUE);

			if (existingSection != null && !existingSection.getId().equals(sectionDto.getId())) {
				throw new BussinessException(HttpStatus.EXPECTATION_FAILED, "Section name already exists");
			}

			section = sectionRepository.findByIdAndActive(sectionDto.getId(), Boolean.TRUE);

			if (section != null) {
				BeanUtils.copyProperties(sectionDto, section);
				section.setModifiedBy(userId);
				section.setModifiedOn(new Date());

			} else {
				section = new Section();
				BeanUtils.copyProperties(sectionDto, section);
				section.setCreatedBy(userId);
				section.setCreatedOn(new Date());
			}
			section = sectionRepository.save(section);
			sectionId = section.getId();

			processSectionElements(sectionDto, sectionId, userId);

			response.put("sectionName", sectionDto.getName());
			response.put("sectionId", sectionId);
			response.put(Constants.SUCCESS, new SuccessResponse("Section saved successfully"));
			response.put(Constants.ERROR, null);
		} catch (RuntimeException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	private void processSectionElements(SectionDto sectionDto, Long sectionId, Long userId)
			throws TechnicalException, BussinessException, ContractException {

		if (!CollectionUtils.isEmpty(sectionDto.getSectionElements())) {

			List<Elements> existingElements = elementsRepository.findBySectionIdAndActive(sectionId, Boolean.TRUE);

			Set<Long> existingElementIds = existingElements.stream().map(Elements::getId).collect(Collectors.toSet());

			Set<Long> newElementIds = sectionDto.getSectionElements().stream().map(ElementsDto::getId)
					.collect(Collectors.toSet());

			Set<Long> elementsToRemove = new HashSet<>(existingElementIds);
			elementsToRemove.removeAll(newElementIds);

			elementsRepository.deleteAllByIdIn(elementsToRemove);

			for (ElementsDto elementDto : sectionDto.getSectionElements()) {
				elementDto.setSectionId(sectionId);
				saveElements(elementDto, userId, sectionDto.getTemplateId());
			}
		} else {
			elementsRepository.deleteBySectionId(sectionId);
		}
	}

	public Map<String, Object> saveElements(ElementsDto elementDto, Long userId, Long template)
			throws TechnicalException, BussinessException, ContractException {

		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> response = new HashMap<>();

		Elements element = getElement(elementDto);

		if (element == null) {
			element = createNewElement(elementDto, userId, template);
		} else {
			element = updateExistingElement(element, elementDto, userId, template);
		}

		element = elementsRepository.save(element);

		saveElementOptions(element, elementDto.getValues());

		response.put("elementId", element.getId());
		response.put(Constants.SUCCESS, new SuccessResponse("Element saved successfully"));
		response.put(Constants.ERROR, null);

		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	private Elements getElement(ElementsDto elementDto) {
		log.info(LogUtil.startLog(CLASSNAME));
		if (elementDto == null || elementDto.getId() == null || elementDto.getSectionId() == null) {
			return null;
		}
		try {
			return elementsRepository.findByIdAndSectionIdAndActive(elementDto.getId(), elementDto.getSectionId(),
					Boolean.TRUE);
		} catch (Exception e) {
			log.error("Error fetching template element", e);
			return null;
		}
	}

	private Elements createNewElement(ElementsDto elementDto, Long userId, Long templateId) {
		log.info(LogUtil.startLog(CLASSNAME));
		Elements element = new Elements();
		BeanUtils.copyProperties(elementDto, element);
		element.setCreatedBy(userId);
		element.setCreatedOn(new Date());
		element.setTemplateId(templateId);
		log.info(LogUtil.exitLog(CLASSNAME));
		return element;
	}

	private Elements updateExistingElement(Elements element, ElementsDto elementDto, Long userId, Long templateId) {
		log.info(LogUtil.startLog(CLASSNAME));
		BeanUtils.copyProperties(elementDto, element);
		element.setModifiedBy(userId);
		element.setModifiedOn(new Date());
		element.setTemplateId(templateId);
		log.info(LogUtil.exitLog(CLASSNAME));
		return element;
	}

	private void saveElementOptions(Elements element, List<ElementOptionsDto> optionsDtoList) {
		if (!CollectionUtils.isEmpty(optionsDtoList)) {
			List<ElementOptions> elementOptionsList = new ArrayList<>();

			for (ElementOptionsDto elementOptionsDto : optionsDtoList) {
				elementOptionsDto.setElementId(element.getId());

				ElementOptions elementOptions = elementOptionsRepository.findByElementIdAndId(element.getId(),
						elementOptionsDto.getId());

				if (elementOptions == null) {
					elementOptions = new ElementOptions();
				}

				BeanUtils.copyProperties(elementOptionsDto, elementOptions, "id");

				elementOptionsList.add(elementOptions);
			}

			elementOptionsRepository.saveAll(elementOptionsList);
		}
	}

	@Override
	public Map<String, Object> getTemplateList(Long categoryId, Long domainId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		Map<String, Object> response = new HashMap<>();
		verifyToken(headers);
		try {
			Set<Long> templateIds = fetchTemplateIds(categoryId, domainId);

			List<TemplateMaster> templates = templateIds.isEmpty() ? templateRepository.findAll()
					: templateRepository.findByIds(new ArrayList<>(templateIds));

			if (templates.isEmpty()) {
				response.put(TEMPLATE_DTO, Collections.emptyList());
				response.put(Constants.SUCCESS, new SuccessResponse("No templates available."));
				response.put(Constants.ERROR, null);
			} else {
				response.put(TEMPLATE_DTO, buildTemplateDtos(templates));
				response.put(Constants.SUCCESS, new SuccessResponse(Constants.SUCCESS));
				response.put(Constants.ERROR, null);
			}
		} catch (Exception e) {
			log.error("Error occurred in getTemplateList: {}", LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	private Set<Long> fetchTemplateIds(Long categoryId, Long domainId) {
		Set<Long> templateIds = new HashSet<>();
		if (categoryId != null && categoryId > 0) {
			templateIds.addAll(templateCategoryMappingRepository.findByCategoryId(categoryId));
		}
		if (domainId != null && domainId > 0) {
			templateIds.addAll(templateDomainMappingRepository.findByDomainId(domainId));
		}
		return templateIds;
	}

	private List<Map<String, Object>> buildTemplateDtos(List<TemplateMaster> templates) {
		return templates.stream().map(template -> {
			Map<String, Object> dto = new HashMap<>();
			dto.put("id", template.getId());
			dto.put("name", template.getName());
			dto.put("templateCategoryList", getCategoryNames(template.getId()));
			dto.put("templateDomainList", getDomainNames(template.getId()));
			return dto;
		}).toList();
	}

	private List<String> getCategoryNames(Long templateId) {
		List<Long> categoryIds = templateCategoryMappingRepository.findByTemplateId(templateId);
		return (categoryIds != null && !categoryIds.isEmpty()) ? categoryRepository.findNamesByIds(categoryIds)
				: Collections.emptyList();
	}

	private List<String> getDomainNames(Long templateId) {
		List<Long> domainIds = templateDomainMappingRepository.findByTemplateId(templateId);
		return (domainIds != null && !domainIds.isEmpty()) ? domainRepository.findNamesByIds(domainIds)
				: Collections.emptyList();
	}

	@Override
	public Map<String, Object> findAllTemplatePagination(List<SequenceColumnDTO> sequenceColumnDTOs, int draw,
			int start, int length, String columns, String search, String sortOrder, String sortField, String searchCol,
			boolean status, MultiValueMap<String, String> headers, Integer templateType)
			throws TechnicalException, BussinessException, ContractException {

		log.info(LogUtil.startLog(CLASSNAME));

		if (ObjectUtils.isZero(length))
			length = Constants.ONE_BILLION;

		PageRequest pageRequest = null;

		Map<String, Object> userResponse = verifyToken(headers);
		Long companyId = Long.parseLong(userResponse.get(Constants.USER_COMPANY_ID) + Constants.EMPTYSTRING);
		String timeZone = String.valueOf(userResponse.get(Constants.USER_TIME_ZONE));
		String zone = timeZone.substring(timeZone.indexOf("(") + 1, timeZone.indexOf(")"));
		log.info("Zzone is  " + zone);
		Map<String, Object> resps = companyServiceFeignProxy.findCountryIdByCompanyId(companyId, headers);
		Long countryId = Long.parseLong(resps.get(Constants.COUNTRY_ID) + Constants.EMPTYSTRING);

		Map<String, Object> resps2 = ExceptionUtil
				.throwExceptionsIfPresent(locationServiceFeignProxy.findDateFormaterByCountryId(countryId));
		String dateFormat = String.valueOf(resps2.get(Constants.DATE_FORMAT));

		String originalSortField = null;
		String originalSortOrder = null;

		if (TEMPLATE_TYPE.equals(sortField)) {
			originalSortField = sortField;
			originalSortOrder = sortOrder;

			sortField = "name";
			sortOrder = "desc";
		}

		if (!StringUtils.isEmpty(sortField)) {
			switch (sortField) {
			case "reviewerName":
				sortField = "reviewer";
				break;
			case "templateUsed":
				sortField = TEMPLATE_TYPE;
				break;
			default:
				break;
			}
		}

		if (sortField.equalsIgnoreCase(Constants.NAME)) {
			pageRequest = PageRequest.of(start, length,
					org.springframework.data.domain.Sort.Direction.fromString(sortOrder), Constants.NAME);
		} else {
			pageRequest = PageRequest.of(start, length,
					org.springframework.data.domain.Sort.Direction.fromString(sortOrder), sortField);
		}

		List<TemplateDto> dtolist = new ArrayList<>();
		List<TemplateMaster> newlist = new ArrayList<>();

		Page<TemplateMaster> list = paginateTemplate(search, searchCol, pageRequest, status, templateType);

		if (list != null) {
			newlist = list.getContent();
		}
		List<SortDto> sortList = determineSortCriteria(sortOrder, sortField, originalSortField, originalSortOrder);
		List<ColumnDetailsDTO> assetTypeColumnsList = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		if (CollectionUtils.isNotEmpty(newlist)) {
			log.info("newlist size" + newlist.size());
			for (TemplateMaster obj : newlist) {

				TemplateDto dto = new TemplateDto();
				BeanUtils.copyProperties(obj, dto);

				if (!searchCol.equals("createdOn")) {
					if (obj.getCreatedOn() != null) {
						dto.setCreatedOn(DateUtil.convertToTimeZoneById(obj.getCreatedOn(), dateFormat, zone));
						log.info("CreatedOn is  "
								+ DateUtil.convertToTimeZoneById(obj.getCreatedOn(), dateFormat, zone));
					} else {
						dto.setCreatedOn("-");
					}
				} else {
					dto.setCreatedOn(DateFormatUtils.format(obj.getCreatedOn(), dateFormat));
				}

				if (!searchCol.equals("modifiedOn")) {
					if (obj.getModifiedOn() != null) {
						dto.setModifiedOn(DateUtil.convertToTimeZoneById(obj.getModifiedOn(), dateFormat, zone));
					} else {
						dto.setModifiedOn("-");
					}
				} else {
					dto.setModifiedOn(DateFormatUtils.format(obj.getModifiedOn(), dateFormat));
				}

				if (ObjectUtils.isPositiveNonZero(obj.getCreatedBy())) {
					UserMasterDTO user = userUtil.getUserById(obj.getCreatedBy(), headers);
					dto.setCreatedBy(user.getFullName());
				} else {
					dto.setCreatedBy(Constants.NA);
				}

				if (ObjectUtils.isPositiveNonZero(obj.getModifiedBy())) {
					UserMasterDTO user = userUtil.getUserById(obj.getModifiedBy(), headers);
					dto.setModifiedBy(user.getFullName());
				} else {
					dto.setModifiedBy(Constants.NA);
				}

				if (ObjectUtils.isPositiveNonZero(obj.getReviewer())) {
					UserMasterDTO user = userUtil.getUserById(obj.getReviewer(), headers);
					dto.setReviewer(user.getFullName());
				} else {
					dto.setReviewer(Constants.NA);
				}

				dto = setCategoryData(obj, dto);
				dto = setDomainData(obj, dto);
				dtolist.add(dto);
			}
		}

		Long totalCount = null;
		if (list != null) {
			totalCount = list.getTotalElements();
		} else {
			totalCount = (long) newlist.size();

		}
		if (CollectionUtils.isNotEmpty(sequenceColumnDTOs)) {
			for (SequenceColumnDTO columnDTO : sequenceColumnDTOs) {
				if (ObjectUtils.isPositiveNonZero(columnDTO.getColumnID())
						&& ObjectUtils.isPositiveNonZero(columnDTO.getSequenceColumn())) {
					ColumnDetailsDTO columnDetails = columnServiceFeignProxy.findBycolumnId(columnDTO.getColumnID());
					if (columnDetails != null) {
						ColumnDetailsDTO columnDetailsDTO = new ColumnDetailsDTO();
						BeanUtils.copyProperties(columnDetails, columnDetailsDTO);
						columnDetailsDTO.setSequenceColumn(columnDTO.getSequenceColumn());
						assetTypeColumnsList.add(columnDetailsDTO);
					}
				}
			}
		} else {
			log.info("columnServiceFeignProxy called");
			assetTypeColumnsList = columnServiceFeignProxy.defaultRender(Constants.NAME);
		}

		if (CollectionUtils.isNotEmpty(assetTypeColumnsList)) {
			map.put(Constants.COLUMN, assetTypeColumnsList);
		} else {
			map.put(Constants.COLUMN, Collections.emptyList());
		}

		sortingDtoList(dtolist, sortList);
		map.put(TEMPLATE_DTO, dtolist);

		if (CollectionUtils.isNotEmpty(dtolist)) {
			map.put(Constants.TOTAL_COUNT, totalCount);
		} else {
			map.put(Constants.TOTAL_COUNT, 0);
		}

		map.put(Constants.SUCCESS, new SuccessResponse("Template listed"));
		map.put(Constants.ERROR, null);
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

	private TemplateDto setCategoryData(TemplateMaster obj, TemplateDto dto) {
		List<TemplateCategoryMapping> templateCategoryMappings = templateCategoryMappingRepository
				.findByTemplateIdAndActive(obj.getId(), true);

		List<Long> categoryIds = templateCategoryMappings.stream().map(TemplateCategoryMapping::getCategoryId).toList();

		List<CategoryMaster> categoryMasterList = categoryRepository.findAllById(categoryIds);

		List<CategoryMasterDto> categoryMasterDtoList = categoryMasterList.stream().map(this::mapToDto).toList();

		dto.setCategories(categoryMasterDtoList);
		return dto;
	}

	private CategoryMasterDto mapToDto(CategoryMaster categoryMaster) {
		if (categoryMaster == null) {
			return null;
		}
		CategoryMasterDto categoryMasterDto = new CategoryMasterDto();
		BeanUtils.copyProperties(categoryMaster, categoryMasterDto);
		categoryMasterDto.setCreatedOn(dynamicDateFormat.format(categoryMaster.getCreatedOn()));
		if (categoryMaster.getModifiedOn() != null) {
			categoryMasterDto.setModifiedOn(dynamicDateFormat.format(categoryMaster.getModifiedOn()));
		}
		return categoryMasterDto;
	}

	private TemplateDto setDomainData(TemplateMaster obj, TemplateDto dto) {
		List<TemplateDomainMapping> templateDomainMappings = templateDomainMappingRepository
				.findByTemplateIdAndActive(obj.getId(), true);

		List<Long> domainIds = templateDomainMappings.stream().map(TemplateDomainMapping::getDomainId)
				.collect(Collectors.toList());

		List<DomainMaster> domainMasterList = domainRepository.findAllById(domainIds);

		List<DomainMasterDto> domainMasterDtoList = domainMasterList.stream().map(this::mapToDomainDto)
				.collect(Collectors.toList());

		dto.setDomains(domainMasterDtoList);
		return dto;
	}

	private DomainMasterDto mapToDomainDto(DomainMaster domainMaster) {
		if (domainMaster == null) {
			return null;
		}
		DomainMasterDto domainMasterDto = new DomainMasterDto();
		BeanUtils.copyProperties(domainMaster, domainMasterDto);
		domainMasterDto.setCreatedOn(dynamicDateFormat.format(domainMaster.getCreatedOn()));
		if (domainMaster.getModifiedOn() != null) {
			domainMasterDto.setModifiedOn(dynamicDateFormat.format(domainMaster.getModifiedOn()));
		}
		return domainMasterDto;
	}

	private void sortingDtoList(List<TemplateDto> dtolist, List<SortDto> sortList) {
		for (SortDto sortDto : sortList) {
			if (TEMPLATE_TYPE.equals(sortDto.getSortField())) {
				if ("asc".equalsIgnoreCase(sortDto.getSortOrder())) {
					dtolist.sort(Comparator.comparing((TemplateDto templateDto) -> templateDto.getTemplateType() != null
							? templateDto.getTemplateType()
							: 0L));
				} else {
					dtolist.sort(Comparator.comparing((TemplateDto templateDto) -> templateDto.getTemplateType() != null
							? templateDto.getTemplateType()
							: 0L).reversed());
				}
			}
		}
	}

	private List<SortDto> determineSortCriteria(String sortOrder, String sortField, String originalSortField,
			String originalSortOrder) {
		if ("name".equals(sortField) && TEMPLATE_TYPE.equals(originalSortField)) {
			sortField = TEMPLATE_TYPE;
			sortOrder = originalSortOrder;
		}

		List<SortDto> sortList = new ArrayList<>();
		SortDto sortDto = new SortDto();
		sortDto.setSortField(sortField);
		sortDto.setSortOrder(sortOrder);
		sortList.add(sortDto);
		return sortList;
	}

	private Page<TemplateMaster> paginateTemplate(String search, String searchcolumn, PageRequest pageRequest,
			boolean status, Integer templateType) throws ContractException, TechnicalException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<TemplateMaster> criteria = builder.createQuery(TemplateMaster.class);
		Root<TemplateMaster> root = criteria.from(TemplateMaster.class);
		List<Predicate> predicates = new ArrayList<>();
		Predicate activepredicate = builder.equal(root.get(Constants.ACTIVE), status);

		Expression<Integer> parentExpression = root.get(TEMPLATE_TYPE);
		Predicate typepredicate = parentExpression.in(templateType);

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
					|| searchcolumn.equals("id") || searchcolumn.equals("reviewer")) {
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
			predicates.add(builder.like(builder.lower(root.get("name")),
					Constants.LIKE_STRING.replace(Constants.LIKE, search.toLowerCase())));
		}
		Predicate searchpredicatevar = builder.and(predicates.toArray(new Predicate[predicates.size()]));

		if (templateType == 0) {
			criteria.where(builder.and(searchpredicatevar, activepredicate));
		} else {
			criteria.where(builder.and(typepredicate, searchpredicatevar, activepredicate));
		}
		criteria.orderBy(QueryUtils.toOrders(pageRequest.getSort(), root, builder));
		List<TemplateMaster> result = em.createQuery(criteria).setFirstResult((int) pageRequest.getOffset())
				.setMaxResults(pageRequest.getPageSize()).getResultList();
		List<TemplateMaster> result1 = em.createQuery(criteria).getResultList();
		int count = result1.size();
		log.info(LogUtil.startLog(CLASSNAME));
		return new PageImpl<>(result, pageRequest, count);
	}

	@Override
	public Map<String, Object> templateListing(ListingDto listingDto, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));

		Map<String, Object> userResponse = verifyToken(headers);
		String timeZone = String.valueOf(userResponse.get(Constants.USER_TIME_ZONE));
		String zone = timeZone.substring(timeZone.indexOf("(") + 1, timeZone.indexOf(")"));
		log.info("Zzone is  " + zone);
		List<SortDto> sort = listingDto.getSort();
		String originalSortField = null;
		String originalSortOrder = null;
		for (SortDto sortDto : sort) {
			if (TEMPLATE_TYPE.equals(sortDto.getSortField())) {
				originalSortField = sortDto.getSortField();
				originalSortOrder = sortDto.getSortOrder();

				sortDto.setSortField("name");
				sortDto.setSortOrder("desc");
			}
		}

		List<TemplateDto> dtolist = new ArrayList<>();
		Page<TemplateMaster> list = TemplatePaginate.paginate(em, listingDto, TemplateMaster.class);
		log.error("TemplateMaster List Count: " + list.getContent().size());

		restoreOriginalSortFields(sort, originalSortField, originalSortOrder);
		List<TemplateMaster> newlist = list.getContent();
		List<ColumnDetailsDTO> templateColumnsList = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		Set<Long> userIds = new HashSet<>();

		if (CollectionUtils.isNotEmpty(newlist)) {
			log.info("newlist size => {}", newlist.size());
			for (TemplateMaster template : newlist) {
				if (ObjectUtils.isPositiveNonZero(template.getCreatedBy())) {
					userIds.add(template.getCreatedBy());
				}
				if (ObjectUtils.isPositiveNonZero(template.getModifiedBy())) {
					userIds.add(template.getModifiedBy());
				}
			}
			Map<String, UserMasterDTO> userDtos = userUtil.getLoggedInUserByIds(userIds, headers);
			for (TemplateMaster obj : newlist) {
				TemplateDto dto = new TemplateDto();
				BeanUtils.copyProperties(obj, dto);

				if (obj.getCreatedOn() != null) {
					dto.setCreatedOn(
							org.apache.commons.lang3.time.DateFormatUtils.format(obj.getCreatedOn(), DATE_FORMAT4));
				}

				if (obj.getModifiedOn() != null) {
					dto.setModifiedOn(
							org.apache.commons.lang3.time.DateFormatUtils.format(obj.getModifiedOn(), DATE_FORMAT4));
				}
				if (obj.getReviewedOn() != null) {
					dto.setReviewedOn(
							org.apache.commons.lang3.time.DateFormatUtils.format(obj.getReviewedOn(), DATE_FORMAT4));

				}
				log.info("userServiceFeignProxy----->");
				dto.setCreatedBy(Paginate.getFullName(userDtos, obj.getCreatedBy()));
				dto.setModifiedBy(Paginate.getFullName(userDtos, obj.getModifiedBy()));
				dto.setReviewer(Paginate.getFullName(userDtos, obj.getReviewer()));
				dto.setStatus(obj.getStatus());
				dto.setTemplateType(obj.getTemplateType().getId());
				dto = setCategoryData(obj, dto);
				dto = setDomainData(obj, dto);
				dtolist.add(dto);
			}
		}

		Long totalCount = list.getTotalElements();
		templateColumnsList = Paginate.getColumns(columnServiceFeignProxy, listingDto, "abc");

		if (CollectionUtils.isEmpty(templateColumnsList)) {
			templateColumnsList = Collections.emptyList();
		}
		map.put(Constants.COLUMN, templateColumnsList);
		sortingDtoList(dtolist, sort);
		map.put(TEMPLATE_DTO, dtolist);
		map.put(Constants.TOTAL_COUNT, CollectionUtils.isNotEmpty(dtolist) ? totalCount : 0);
		map.put(Constants.SORT, listingDto.getSort());
		map.put(Constants.SEARCH, listingDto.getSearch());
		map.put(Constants.DEFAULT_SORT, listingDto.getDefaultSort());
		map.put(Constants.ERROR, null);
		map.put(Constants.SUCCESS, new SuccessResponse("listed successfully"));
		log.info(LogUtil.exitLog(this.getClass().getName()));
		return map;

	}

	private void restoreOriginalSortFields(List<SortDto> sort, String originalSortField, String originalSortOrder) {
		for (SortDto sortDto : sort) {
			if ("name".equals(sortDto.getSortField()) && TEMPLATE_TYPE.equals(originalSortField)) {
				sortDto.setSortField(originalSortField);
				sortDto.setSortOrder(originalSortOrder);
			}

		}
	}

	@Override
	public Map<String, Object> getAllUsersForTemplates(MultiValueMap<String, String> headers)
			throws TechnicalException, ContractException, BussinessException {
		Map<String, Object> response = new HashMap<>();
		try {
			verifyToken(headers);
			List<TemplateMaster> templates = templateRepository.findByActive(true);

			List<Long> combinedIds = Stream
					.concat(templates.stream().map(TemplateMaster::getCreatedBy).filter(Objects::nonNull),
							templates.stream().map(TemplateMaster::getReviewer).filter(Objects::nonNull))
					.distinct() // Remove duplicates
					.toList();

			log.error("UserIds: " + combinedIds);

			if (combinedIds.isEmpty()) {
				response.put(USER_DTO, Collections.emptyList());
				response.put(Constants.SUCCESS, new SuccessResponse("No Users available."));
				response.put(Constants.ERROR, null);
				return response;
			}
			List<UserMasterDTO> userlist = userUtil.getUserDetailsByUserIds(combinedIds, headers);
			response.put(USER_DTO, userlist);
			response.put(Constants.SUCCESS, new SuccessResponse(Constants.SUCCESS));
			response.put(Constants.ERROR, null);

		} catch (Exception e) {
			log.error("Error occurred in getAllUsersForTemplates: {}", LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public Map<String, Object> deleteTemplateMaster(Long id, boolean active, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> response = new HashMap<>();
		Optional<TemplateMaster> templateMaster;
		Map<String, Object> userResponse = verifyToken(headers);
		try {
			String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));
			TemplateMasterValidator.isPrimaryIdPresent(id, language);

			templateMaster = templateRepository.findById(id);
			if (templateMaster.isEmpty()) {
				log.error(EnglishConstants.TM0002);
				throw new BussinessException(HttpStatus.NOT_ACCEPTABLE, ConstantsUtil.getConstant("TM0002", language));
			}
			templateMaster.get().setActive(active);
			templateRepository.save(templateMaster.get());
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
	@Transactional
	public Map<String, Object> saveTemplateRule(TemplateRulesDto templateRulesDto,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException {

		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> response = new HashMap<>();
		Map<String, Object> verifyToken = verifyToken(headers);
		Long loggedInUserId = Long.parseLong(String.valueOf(verifyToken.get(Constants.USER_MASTER_ID)));

		try {

			TemplateRules rules = templateRulesRepository.findByIdAndActive(templateRulesDto.getId(), Boolean.TRUE);

			if (rules != null) {
				BeanUtils.copyProperties(templateRulesDto, rules);
				rules.setModifiedBy(loggedInUserId);
				rules.setModifiedOn(new Date());
			} else {
				rules = new TemplateRules();
				BeanUtils.copyProperties(templateRulesDto, rules);
				rules.setCreatedBy(loggedInUserId);
				rules.setCreatedOn(new Date());
			}

			Long templateRuleId = templateRulesRepository.save(rules).getId();

			List<TemplateRulesWhen> templateRulesWhenList = saveTemplateRulesWhen(templateRulesDto.getWhen(),
					templateRuleId, loggedInUserId);
			templateRulesWhenRepository.saveAll(templateRulesWhenList);

			List<TemplateRulesThen> templateRulesThenList = saveTemplateRulesThen(templateRulesDto.getThen(),
					templateRuleId, loggedInUserId);
			templateRulesThenRepository.saveAll(templateRulesThenList);

			response.put(Constants.STATUS, "success");
			response.put("message", "Template rule and conditions saved successfully");
			response.put("templateRuleId", templateRuleId);

		} catch (Exception e) {
			log.error("Technical exception occurred: {}", LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	private List<TemplateRulesWhen> saveTemplateRulesWhen(List<TemplateRulesWhenDto> whenDtos, Long templateRuleId,
			Long loggedInUserId) {
		List<TemplateRulesWhen> whenList = new ArrayList<>();
		for (TemplateRulesWhenDto whenDto : whenDtos) {

			TemplateRulesWhen whenEntity = templateRulesWhenRepository.findByIdAndActive(whenDto.getId(), Boolean.TRUE);

			if (whenEntity != null) {
				BeanUtils.copyProperties(whenDto, whenEntity);
				whenEntity.setTemplateRuleId(templateRuleId);
				whenEntity.setModifiedBy(loggedInUserId);
				whenEntity.setModifiedOn(new Date());

			} else {
				whenEntity = new TemplateRulesWhen();
				BeanUtils.copyProperties(whenDto, whenEntity);
				whenEntity.setTemplateRuleId(templateRuleId);
				whenEntity.setCreatedBy(loggedInUserId);
				whenEntity.setCreatedOn(new Date());
			}

			whenList.add(whenEntity);
		}
		return whenList;
	}

	private List<TemplateRulesThen> saveTemplateRulesThen(List<TemplateRulesThenDto> thenDtos, Long templateRuleId,
			Long loggedInUserId) {
		List<TemplateRulesThen> thenList = new ArrayList<>();
		for (TemplateRulesThenDto thenDto : thenDtos) {

			TemplateRulesThen thenEntity = templateRulesThenRepository.findByIdAndActive(thenDto.getId(), Boolean.TRUE);

			if (thenEntity != null) {
				BeanUtils.copyProperties(thenDto, thenEntity);
				thenEntity.setTemplateRuleId(templateRuleId);
				thenEntity.setModifiedBy(loggedInUserId);
				thenEntity.setModifiedOn(new Date());

			} else {
				thenEntity = new TemplateRulesThen();
				BeanUtils.copyProperties(thenDto, thenEntity);
				thenEntity.setTemplateRuleId(templateRuleId);
				thenEntity.setCreatedBy(loggedInUserId);
				thenEntity.setCreatedOn(new Date());
			}

			thenList.add(thenEntity);
		}
		return thenList;
	}

	@Override
	@CustomTransactional
	public Map<String, Object> copyTemplate(TemplateDto templateDto, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException {

		log.info(LogUtil.startLog(CLASSNAME));

		Map<String, Object> response = new HashMap<>();

		Map<String, Object> userResponse = ExceptionUtil.throwExceptionsIfPresent(userUtil.tokenVerification(headers));

		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
		String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));
		Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

		TemplateMaster template = new TemplateMaster();
		Integer versionNo = 0;
		Long newTemplateId = null;

		try {

			TemplateMasterValidator.dtoValidationUtil(templateDto, language);

			uniqueNameCheck(Boolean.TRUE, templateDto);

			if (template.getVersionNumber() != null && template.getVersionNumber() > 0) {
				versionNo = template.getVersionNumber();
			}

			BeanUtils.copyProperties(templateDto, template);

			setTemplateProperties(template, templateDto, userId, companyId, versionNo);

			template = templateRepository.save(template);
			newTemplateId = template.getId();

			cloneTemplate(headers, newTemplateId, templateDto.getTemplateId());

			response.put("TemplateId : ", newTemplateId);
			response.put(Constants.SUCCESS, new SuccessResponse("Template Copied Successfully"));
			response.put(Constants.ERROR, null);

		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}

		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	public void uniqueNameCheck(Boolean isEdit, TemplateDto templateDto) throws ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		TemplateMaster template = templateRepository.findByNameAndActive(templateDto.getName(), Boolean.TRUE);
		if (isEdit.equals(Boolean.TRUE)) {
			if (template != null && !template.getId().equals(templateDto.getId())) {
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
						"Name : " + templateDto.getName() + ", Already Exist");
			}
		} else {
			if (template != null) {
				throw new ContractException(HttpStatus.NOT_ACCEPTABLE,
						"Name : " + templateDto.getName() + ", Already Exist");
			}
		}
		log.info(LogUtil.exitLog(CLASSNAME));
	}

	private void setTemplateProperties(TemplateMaster template, TemplateDto templateDto, Long userId, Long companyId,
			Integer versionNo) {
		log.info(LogUtil.startLog(CLASSNAME));
		template.setCompanyId(companyId);
		template.setCreatedBy(userId);
		template.setCreatedOn(new Date());
		template.setVersionNumber(versionNo + 1);
		template.setTemplateType(findTemplateTypeById(templateDto.getTemplateType()));
		template.setSelfReview(false);
		if (templateDto.getSelfReview() != null && templateDto.getSelfReview().equals(Boolean.TRUE)) {
			template.setReviewer(userId);
		}

		log.info(LogUtil.exitLog(CLASSNAME));
	}

	private void cloneTemplate(MultiValueMap<String, String> headers, Long newTemplateId, Long templateId)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.startLog(CLASSNAME));
		try {
			Map<String, Object> map = getTemplate(templateId, headers);

			if (map != null && map.containsKey(TEMPLATE_DTO)) {
				TemplateDto dto = new ObjectMapper().readValue(new Gson().toJson(map.get(TEMPLATE_DTO)),
						new TypeReference<TemplateDto>() {
						});

				for (SectionDto sectionsDto : dto.getSections()) {
					sectionsDto.setId(null);
					sectionsDto.setTemplateId(newTemplateId);
					for (ElementsDto templateElementDto : sectionsDto.getSectionElements()) {
						templateElementDto.setId(null);
						templateElementDto.setTemplateId(newTemplateId);
					}
					saveTemplateSection(sectionsDto, headers);

				}
			}
		} catch (Exception e) {
			log.error("An error occurred while cloning template template: ", e);
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
					Constants.EMPTYSTRING);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
	}

	@Override
	@Transactional
	public Map<String, Object> saveTemplate(TemplateDto templateDto, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {

		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> response = new HashMap<>();

		try {
			Map<String, Object> userResponse = verifyToken(headers);
			Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
			String language = String.valueOf(userResponse.get(Constants.USER_LANGUAGE));
			Long companyId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_COMPANY_ID)));

			TemplateMasterValidator.dtoValidationUtil(templateDto, language);

			TemplateMaster templateMaster = processTemplate(templateDto, userId, companyId);

			response.put(TEMPLATE_DTO, getTemplate(templateMaster.getId(), headers).get(TEMPLATE_DTO));
			response.put(TEMPLATE_ID, templateMaster.getId());
			response.put(Constants.SUCCESS, new SuccessResponse(getSuccessMessage(templateDto)));
			response.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error("Error in saveTemplate: ", e);
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		log.info("Exiting saveTemplate method");
		return response;
	}

	private TemplateMaster processTemplate(TemplateDto templateDto, Long userId, Long companyId)
			throws ContractException {

		TemplateMaster templateMaster = templateRepository.findByIdAndActive(templateDto.getId(), Boolean.TRUE);

		int versionNo = (templateMaster != null && templateMaster.getVersionNumber() != null)
				? templateMaster.getVersionNumber()
				: 0;

		if (templateMaster != null) {
			updateExistingTemplate(templateDto, templateMaster, userId, companyId, versionNo);
		} else {
			templateMaster = createNewTemplate(templateDto, userId, companyId, versionNo);
		}
		mapCategotysAndDomais(templateMaster.getId(), userId, templateDto);
		return templateMaster;
	}

	private void updateExistingTemplate(TemplateDto templateDto, TemplateMaster templateMaster, Long userId,
			Long companyId, int versionNo) throws ContractException {
		uniqueNameCheck(Boolean.TRUE, templateDto);
		BeanUtils.copyProperties(templateDto, templateMaster);

		templateMaster.setVersionNumber(versionNo + 1);
		templateMaster.setSelfReview(templateDto.getSelfReview());
		if (Boolean.TRUE.equals(templateDto.getSelfReview())) {
			templateMaster.setReviewer(userId);
			templateMaster.setReviewedOn(new Date());
		}
		templateMaster.setModifiedBy(userId);
		templateMaster.setCompanyId(companyId);
		templateMaster.setModifiedOn(new Date());
		templateRepository.save(templateMaster);
	}

	private TemplateMaster createNewTemplate(TemplateDto templateDto, Long userId, Long companyId, int versionNo)
			throws ContractException {
		uniqueNameCheck(Boolean.FALSE, templateDto);
		TemplateMaster templateMaster = new TemplateMaster();
		BeanUtils.copyProperties(templateDto, templateMaster);

		templateMaster.setTemplateType(findTemplateTypeById(templateDto.getTemplateType()));
		templateMaster.setVersionNumber(versionNo + 1);
		templateMaster.setSelfReview(templateDto.getSelfReview());
		if (Boolean.TRUE.equals(templateDto.getSelfReview())) {
			templateMaster.setReviewer(userId);
			templateMaster.setReviewedOn(new Date());
		}
		templateMaster.setStatus(templateDto.getStatus());
		templateMaster.setCompanyId(companyId);
		templateMaster.setCreatedBy(userId);
		templateMaster.setCreatedOn(new Date());
		templateMaster = templateRepository.save(templateMaster);

		createDefaultSection(templateMaster.getId(), userId);

		return templateMaster;
	}

	private void mapCategotysAndDomais(Long templateId, Long userId, TemplateDto templateDto) {
		templateDomainMappingRepository.deleteByTemplateId(templateId);
		templateCategoryMappingRepository.deleteByTemplateId(templateId);

		if (CollectionUtils.isNotEmpty(templateDto.getDomainIds())) {
			for (Long domainId : templateDto.getDomainIds()) {
				TemplateDomainMapping domainMapping = new TemplateDomainMapping();
				domainMapping.setDomainId(domainId);
				domainMapping.setTemplateId(templateId);
				domainMapping.setCreatedOn(new Date());
				domainMapping.setCreatedBy(userId);
				domainMapping.setActive(Boolean.TRUE);
				templateDomainMappingRepository.save(domainMapping);
			}
		}

		if (CollectionUtils.isNotEmpty(templateDto.getCategoryIds())) {
			for (Long categoryId : templateDto.getCategoryIds()) {
				TemplateCategoryMapping categoryMapping = new TemplateCategoryMapping();
				categoryMapping.setCategoryId(categoryId);
				categoryMapping.setTemplateId(templateId);
				categoryMapping.setCreatedOn(new Date());
				categoryMapping.setCreatedBy(userId);
				categoryMapping.setActive(Boolean.TRUE);
				templateCategoryMappingRepository.save(categoryMapping);
			}
		}

	}

	private void createDefaultSection(Long templateId, Long userId) {
		Section defaultSection = Section.builder().active(Boolean.TRUE).templateId(templateId).createdBy(userId)
				.createdOn(new Date()).sequence(1L).name(SECTION_1).build();
		sectionRepository.save(defaultSection);
	}

	private String getSuccessMessage(TemplateDto templateDto) {
		return (templateDto.getId() != null && templateDto.getId() > 0) ? "Template Updated Successfully"
				: "Template Saved Successfully";
	}

	private TemplateType findTemplateTypeById(Integer id) {
		if (id == null) {
			return null;
		}
		for (TemplateType type : TemplateType.values()) {
			if (type.getId().equals(id)) {
				return type;
			}
		}
		return null;
	}

	private String formatDate(Date date) {
		return date != null ? org.apache.commons.lang3.time.DateFormatUtils.format(date, DATE_FORMAT) : null;
	}

	@Override
	public Map<String, Object> deleteSectionBySectionId(Long sectionId, boolean status,
			MultiValueMap<String, String> headers) throws TechnicalException, BussinessException, ContractException {

		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> response = new HashMap<>();
		try {
			Section section = sectionRepository.findByIdAndActive(sectionId, Boolean.TRUE);

			if (section == null)
				throw new BussinessException(HttpStatus.NOT_FOUND, "Section not found");

			section.setActive(status);
			sectionRepository.save(section);
			response.put(Constants.SUCCESS, new SuccessResponse("Section deleted successfully"));
			response.put(Constants.ERROR, null);
		} catch (NullPointerException | NonTransientDataAccessException | RecoverableDataAccessException
				| ScriptException | TransientDataAccessException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	@Override
	@Transactional
	public Map<String, Object> saveAllSections(List<SectionDto> sectionDtoList, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException {

		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> userResponse = verifyToken(headers);
		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
		Map<String, Object> response = new HashMap<>();
		List<Map<String, Object>> savedSections = new ArrayList<>();

		try {
			for (SectionDto sectionDto : sectionDtoList) {
				Section section;
				Long sectionId;

				Section existingSection = sectionRepository.findByNameAndTemplateIdAndActive(sectionDto.getName(),
						sectionDto.getTemplateId(), Boolean.TRUE);

				if (existingSection != null && !existingSection.getId().equals(sectionDto.getId())) {
					throw new BussinessException(HttpStatus.EXPECTATION_FAILED,
							"Section name already exists: " + sectionDto.getName());
				}

				section = sectionRepository.findByIdAndActive(sectionDto.getId(), Boolean.TRUE);

				if (section != null) {
					BeanUtils.copyProperties(sectionDto, section);
					section.setModifiedBy(userId);
					section.setModifiedOn(new Date());
				} else {
					section = new Section();
					BeanUtils.copyProperties(sectionDto, section);
					section.setCreatedBy(userId);
					section.setCreatedOn(new Date());
				}
				section = sectionRepository.save(section);
				sectionId = section.getId();

				processSectionElements(sectionDto, sectionId, userId);

				Map<String, Object> sectionResponse = new HashMap<>();
				sectionResponse.put("sectionName", sectionDto.getName());
				sectionResponse.put("sectionId", sectionId);
				savedSections.add(sectionResponse);
			}

			response.put("sections", savedSections);
			response.put(Constants.SUCCESS, new SuccessResponse("Sections saved successfully"));
			response.put(Constants.ERROR, null);
		} catch (RuntimeException e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return response;
	}

	@Override
	public Map<String, Object> deleteManageRule(Long ruleId, Long templateId, Boolean status,
			MultiValueMap<String, String> headers) throws ContractException, TechnicalException, BussinessException {
		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> userResponse = verifyToken(headers);
		Long userId = Long.parseLong(String.valueOf(userResponse.get(Constants.USER_MASTER_ID)));
		try {
			TemplateRules rule = templateRulesRepository.findByIdAndTemplateId(ruleId, templateId);
			if (rule == null) {
				log.warn("{} - Rule with ruleId: {} and templateId: {} not found", CLASSNAME, ruleId, templateId);
				throw new BussinessException(HttpStatus.NOT_FOUND, "Rule not found");
			}
			rule.setActive(status);
			rule.setModifiedBy(userId);
			rule.setModifiedOn(new Date());
			templateRulesRepository.save(rule);

			List<TemplateRulesThen> ruleThen = templateRulesThenRepository.findByTemplateIdAndTemplateRuleId(templateId,
					ruleId);
			List<TemplateRulesWhen> ruleWhen = templateRulesWhenRepository.findByTemplateIdAndTemplateRuleId(templateId,
					ruleId);

			if (CollectionUtils.isNotEmpty(ruleThen)) {
				ruleThen.forEach(thenCondition -> {
					thenCondition.setActive(status);
					thenCondition.setModifiedBy(userId);
					thenCondition.setModifiedOn(new Date());
				});
				templateRulesThenRepository.saveAll(ruleThen);
			}

			if (CollectionUtils.isNotEmpty(ruleWhen)) {
				ruleWhen.forEach(whenCondition -> {
					whenCondition.setActive(status);
					whenCondition.setModifiedBy(userId);
					whenCondition.setModifiedOn(new Date());
				});
				templateRulesWhenRepository.saveAll(ruleWhen);
			}

			String successMessage = Boolean.TRUE.equals(status) ? "Rule activated successfully"
					: "Rule deactivated successfully";
			map.put(Constants.SUCCESS, new SuccessResponse(successMessage));
			map.put(Constants.ERROR, null);
		} catch (Exception e) {
			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

}
