package com.ehr.companymanagement.presentation.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ehr.companymanagement.business.dto.CompanyDetailsDTO;
import com.ehr.companymanagement.business.service.CompanyDetailsService;
import com.ehr.core.dto.SequenceColumnDTO;
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
public class CompanyDetailsController {

	private static final String COMPANY_ID = " companyId ";
	private static final String APPLICATION_JSON_TEXT_PLAIN = "application/json, text/plain,";
	private static final String ACCEPT = "Accept";
	
	private final  CompanyDetailsService companyDetailsService;

	@PostMapping(value = { "/saveCompanyDetails" })
	public Map<String, Object> saveCompanyDetails(@RequestHeader MultiValueMap<String, String> headers,
			@RequestBody final CompanyDetailsDTO companyDetailsDTO)
			throws BussinessException, ContractException, TechnicalException, IOException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.SAVE_COMPANY_DETAILS + " companyDetailsDTO " + companyDetailsDTO.toString()));
		headers.remove("content-length");
		headers.remove(ACCEPT);
		headers.add(ACCEPT, APPLICATION_JSON_TEXT_PLAIN);
		return companyDetailsService.saveCompanyDetails(companyDetailsDTO, headers);
	}

	@PostMapping(value = { "/findCompanyDetailsById" })
	public Map<String, Object> findCompanyDetailsById(@RequestParam(Constants.ID) Long id,
			@RequestHeader MultiValueMap<String, String> headers) throws BussinessException, ContractException,
			TechnicalException, IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.FIND_COMPANY_DETAILS_BY_ID + " id " + id));
		return companyDetailsService.findCompanyDetailsById(id, headers);
	}


	@PostMapping(value = { EndPointReference.GET_ALL_COMPANY_DETAILS_PAGINATION })
	public Map<String, Object> getAllCompanyDetailsPagination(
			@RequestBody(required = false) List<SequenceColumnDTO> sequenceColumnDTOs,
			@RequestParam(defaultValue = "0", value = "draw", required = false) int draw,
			@RequestParam(defaultValue = "0", value = "start", required = false) int start,
			@RequestParam(defaultValue = "0", value = "length", required = false) int length,
			@RequestParam(defaultValue = Constants.EMPTYSTRING, value = Constants.COLUMN, required = false) String columns,
			@RequestParam(defaultValue = Constants.EMPTYSTRING, value = Constants.SEARCH, required = false) String search,
			@RequestParam(defaultValue = "desc", value = "sortOrder", required = false) String sortOrder,
			@RequestParam(defaultValue = Constants.ID, value = "sortField", required = false) String sortField,
			@RequestParam(defaultValue = Constants.EMPTYSTRING, value = "searchCol", required = false) String searchCol,
			@RequestParam(defaultValue = "true", value = Constants.STATUS, required = false) boolean status,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		headers.remove(ACCEPT);
		headers.add(ACCEPT, APPLICATION_JSON_TEXT_PLAIN);
		log.info(LogUtil.presentationLogger(EndPointReference.GET_ALL_COMPANY_DETAILS_PAGINATION));
		return companyDetailsService.findAllCompanyDetailsByPaginationCriteria(sequenceColumnDTOs, draw, start, length,
				columns, search, sortOrder, sortField, searchCol, status, headers);
	}


	@PostMapping(value = { EndPointReference.GET_COMPANY_SUBSCRIPTION_PLAIN_VALIDITY })
	public Map<String, Object> getCompanySubscriptionPlainValidity(@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, ContractException, BussinessException {
		log.info(LogUtil.presentationLogger(EndPointReference.GET_COMPANY_SUBSCRIPTION_PLAIN_VALIDITY));
		return companyDetailsService.getCompanySubscriptionPlainValidity(headers);
	}

	@PostMapping(value = { EndPointReference.GET_LOGO_BY_COMPANY_ID })
	public Map<String, Object> getLogoByCompanyId(@RequestParam(Constants.ID) Long id) throws TechnicalException,
			ContractException, BussinessException, IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.GET_LOGO_BY_COMPANY_ID + " id " + id));
		return companyDetailsService.getLogoByCompanyId(id);
	}

	@PostMapping(value = { EndPointReference.GET_ALL_COMPANY_DETAILS_PAGINATION_BY_EFFECTIVE_FROM_AND_TILL })
	public Map<String, Object> getAllCompanyDetailsPaginationByEffectiveFromAndTill(
			@RequestBody(required = false) List<SequenceColumnDTO> sequenceColumnDTOs,
			@RequestParam(defaultValue = "0", value = "draw", required = false) int draw,
			@RequestParam(defaultValue = "0", value = "start", required = false) int start,
			@RequestParam(defaultValue = "0", value = "length", required = false) int length,
			@RequestParam(defaultValue = Constants.EMPTYSTRING, value = Constants.COLUMN, required = false) String columns,
			@RequestParam(defaultValue = Constants.EMPTYSTRING, value = Constants.SEARCH, required = false) String search,
			@RequestParam(defaultValue = "desc", value = "sortOrder", required = false) String sortOrder,
			@RequestParam(defaultValue = Constants.ID, value = "sortField", required = false) String sortField,
			@RequestParam(defaultValue = Constants.EMPTYSTRING, value = "searchCol", required = false) String searchCol,
			@RequestParam(defaultValue = "true", value = "isEffective", required = false) boolean isEffective,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException {
		headers.remove(ACCEPT);
		headers.add(ACCEPT, APPLICATION_JSON_TEXT_PLAIN);
		log.info(LogUtil
				.presentationLogger(EndPointReference.GET_ALL_COMPANY_DETAILS_PAGINATION_BY_EFFECTIVE_FROM_AND_TILL));
		return companyDetailsService.getAllCompanyDetailsPaginationByEffectiveFromAndTill(sequenceColumnDTOs, draw,
				start, length, columns, search, sortOrder, sortField, searchCol, isEffective, headers);
	}

	@PostMapping(value = { EndPointReference.GET_COMPANY_SUBSCRIPTION_PLAN_STATUS })
	public Map<String, Object> getCompanySubscriptionPlanStatus(@RequestParam(Constants.ID) Long id)
			throws TechnicalException {
		log.info(LogUtil.presentationLogger(EndPointReference.GET_COMPANY_SUBSCRIPTION_PLAN_STATUS + " id " + id));
		return companyDetailsService.getCompanySubscriptionPlanStatus(id);
	}

	@PostMapping(value = { EndPointReference.FIND_COUNTRY_ID_BY_COMPANY_ID })
	public Map<String, Object> findCountryIdByCompanyId(@RequestParam(Constants.COMPANY_ID) Long companyId,
			@RequestHeader MultiValueMap<String, String> headers) throws BussinessException, ContractException, TechnicalException, IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.FIND_COUNTRY_ID_BY_COMPANY_ID + COMPANY_ID + companyId));
		return companyDetailsService.findCountryIdByCompanyId(companyId,headers);
	}

	@PostMapping(value = { EndPointReference.VALIDATE_COMPANY_ID })
	public Map<String, Object> validateCompanyId(@RequestParam(Constants.COMPANY_ID) Long companyId,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		log.info(LogUtil.presentationLogger(EndPointReference.VALIDATE_COMPANY_ID + COMPANY_ID + companyId));
		return companyDetailsService.validateCompanyId(companyId, headers);
	}

	@PostMapping(value = { EndPointReference.GET_COMPANY_NAME_BY_COMPANY_ID })
	public Map<String, Object> getCompanyNameByCompanyId(@RequestParam("companyId") Long companyId,
			@RequestHeader MultiValueMap<String, String> headers)
			throws ContractException, BussinessException, TechnicalException {
		log.info(LogUtil
				.presentationLogger(EndPointReference.GET_COMPANY_NAME_BY_COMPANY_ID + COMPANY_ID + companyId));
		return companyDetailsService.getCompanyNameByCompanyId(companyId);
	}
	
	@PostMapping(value = { "/deleteCompanyDetailsById" })
	public Map<String, Object> deleteCompanyById(final Long id,
			@RequestParam(defaultValue = "false", value = Constants.STATUS, required = false) boolean status,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		log.info(LogUtil.presentationLogger("/deleteCompanyDetailsById" + " id " + id + ", status " + status));
		return companyDetailsService.deleteCompanyById(id, status, headers);
	}
	
	@PostMapping(value = { "/findCompanyNameById" })
	public Map<String, Object> findCompanyNameById(@RequestParam(Constants.ID) Long id,
			@RequestHeader MultiValueMap<String, String> headers) throws BussinessException, ContractException,
			TechnicalException, IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.FIND_COMPANY_DETAILS_BY_ID + " id " + id));
		return companyDetailsService.findCompanyNameById(id, headers);
	
	}
}
