package com.ehr.assessment.presentation.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ehr.assessment.business.dto.SectionDto;
import com.ehr.assessment.business.dto.TemplateDto;
import com.ehr.assessment.business.dto.TemplateRulesDto;
import com.ehr.assessment.business.service.TemplateService;
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
public class TemplateController {

	private final TemplateService templateService;

	@PostMapping({ EndPointReference.GET_TEMPLATE_BY_TEMPLATE_ID })
	public Map<String, Object> getTemplate(@PathVariable("templateId") Long templateId,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException {
		log.info(LogUtil
				.presentationLogger(EndPointReference.GET_TEMPLATE_BY_TEMPLATE_ID + " templateId: " + templateId));
		return templateService.getTemplate(templateId, headers);
	}

	@PostMapping({ EndPointReference.GET_TEMPLATE_LIST })
	public Map<String, Object> getTemplateListForDropDown(@RequestParam("categoryId") Long categoryId,
			@RequestParam("domainId") Long domainId, @RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.presentationLogger(EndPointReference.GET_TEMPLATE_LIST + " categoryId: " + categoryId));
		return templateService.getTemplateList(categoryId, domainId, headers);
	}

	@PostMapping(EndPointReference.FIND_ALL_TEMPLATE_PAGINATION)
	public Map<String, Object> templateListing(@RequestBody ListingDto listingDto,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.FIND_ALL_TEMPLATE_PAGINATION + " listingDto=" + listingDto.toString()));
		return templateService.templateListing(listingDto, headers);
	}

	@PostMapping({ EndPointReference.GET_ALL_USERS_FOR_TEMPLATES })
	public Map<String, Object> getAllUsersForTemplates(@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.presentationLogger(EndPointReference.GET_ALL_USERS_FOR_TEMPLATES));
		return templateService.getAllUsersForTemplates(headers);
	}

	@PostMapping(EndPointReference.DELETE_TEMPLATE_MASTER)
	public Map<String, Object> deleteTemplateMaster(@RequestParam("id") Long id,
			@RequestParam(defaultValue = "false", value = Constants.STATUS, required = false) boolean status,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.DELETE_TEMPLATE_MASTER + " templateMaster " + id + ", status " + status));
		return templateService.deleteTemplateMaster(id, status, headers);
	}

	@PostMapping(value = { EndPointReference.SAVE_TEMPLATE_RULE })
	public Map<String, Object> saveTemplateRule(@RequestBody TemplateRulesDto templateRulesDto,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil
				.presentationLogger(EndPointReference.SAVE_TEMPLATE_RULE + " templateRulesDto: " + templateRulesDto));
		return templateService.saveTemplateRule(templateRulesDto, headers);
	}

	@PostMapping(value = { EndPointReference.SAVE_TEMPLATE_SECTION })
	public Map<String, Object> saveTemplateSection(@RequestBody SectionDto sectionDto,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.SAVE_TEMPLATE_SECTION));
		return templateService.saveTemplateSection(sectionDto, headers);
	}

	@PostMapping(value = { EndPointReference.SAVE_COPY_TEMPLETE })
	public Map<String, Object> copyTemplate(@RequestBody TemplateDto templateDto,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, ParseException, IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.SAVE_COPY_TEMPLETE + "templateDto : " + templateDto));
		return templateService.copyTemplate(templateDto, headers);
	}

	@PostMapping(value = { EndPointReference.SAVE_TEMPLATE })
	public Map<String, Object> saveTemplateSection(@RequestBody TemplateDto templateDto,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException, ParseException {
		log.info(LogUtil.presentationLogger(EndPointReference.SAVE_TEMPLATE_SECTION));
		return templateService.saveTemplate(templateDto, headers);
	}

	@PostMapping(value = { EndPointReference.DELETE_SECTION_BY_SECTION_ID })
	public Map<String, Object> deleteSectionBySectionId(@RequestParam("sectionId") Long sectionId,
			@RequestParam(defaultValue = "false", value = Constants.STATUS, required = false) boolean status,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.presentationLogger(EndPointReference.DELETE_SECTION_BY_SECTION_ID));
		return templateService.deleteSectionBySectionId(sectionId, status, headers);
	}

	@PostMapping(value = { EndPointReference.SAVE_ALL_SECTION })
	public Map<String, Object> saveAllSections(@RequestBody List<SectionDto> sectionDtoList,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.SAVE_ALL_SECTION));
		return templateService.saveAllSections(sectionDtoList, headers);
	}

	@PostMapping(EndPointReference.DELETE_MANAGE_RULE)
	public Map<String, Object> deleteManageRule(@RequestParam("ruleId") Long ruleId,
			@RequestParam("templateId") Long templateId,
			@RequestParam(defaultValue = "false", value = Constants.STATUS, required = false) boolean status,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, TechnicalException, ContractException {
		log.info(LogUtil.presentationLogger(EndPointReference.DELETE_MANAGE_RULE));
		return templateService.deleteManageRule(ruleId, templateId, status, headers);
	}

}
