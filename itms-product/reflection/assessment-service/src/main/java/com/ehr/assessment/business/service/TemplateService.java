package com.ehr.assessment.business.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;

import com.ehr.assessment.business.dto.SectionDto;
import com.ehr.assessment.business.dto.TemplateDto;
import com.ehr.assessment.business.dto.TemplateRulesDto;
import com.ehr.core.dto.ListingDto;
import com.ehr.core.dto.SequenceColumnDTO;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;

public interface TemplateService {

	Map<String, Object> saveTemplate(TemplateDto template, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException;

	Map<String, Object> getTemplate(Long templateId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException;

	Map<String, Object> findAllTemplatePagination(List<SequenceColumnDTO> sequenceColumnDTOs, int draw, int start,
			int length, String columns, String search, String sortOrder, String sortField, String searchCol,
			boolean status, MultiValueMap<String, String> headers, Integer templateType)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> saveTemplateSection(SectionDto sectionDto, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException;

	Map<String, Object> getTemplateList(Long categoryId, Long domainId, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> templateListing(ListingDto listingDto, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException;

	Map<String, Object> getAllUsersForTemplates(MultiValueMap<String, String> headers)
			throws TechnicalException, ContractException, BussinessException;

	Map<String, Object> deleteTemplateMaster(Long id, boolean status, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException;

	Map<String, Object> saveTemplateRule(TemplateRulesDto templateRulesDto, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	List<TemplateRulesDto> getTemplateRules(Long templateId)
			throws BussinessException, ContractException, TechnicalException;

	Map<String, Object> copyTemplate(TemplateDto templateDto, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException;

	Map<String, Object> deleteSectionBySectionId(Long sectionId, boolean status, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> saveAllSections(List<SectionDto> sectionDtoList, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException;

	Map<String, Object> deleteManageRule(Long ruleId, Long templateId, Boolean status,
			MultiValueMap<String, String> headers) throws ContractException, TechnicalException, BussinessException;

}
