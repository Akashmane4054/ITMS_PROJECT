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

import com.ehr.companymanagement.business.dto.SubscriptionPlansDto;
import com.ehr.companymanagement.business.service.SubscriptionPlansService;
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
public class SubscriptionPlansController {

	private SubscriptionPlansService subscriptionPlansService;
	
	@PostMapping(value = { EndPointReference.SAVE_SUBSCRIPTION_PLANS })
	public Map<String, Object> saveSubscriptionPlans(@RequestHeader MultiValueMap<String, String> headers,
			@RequestBody final SubscriptionPlansDto subscriptionPlansDto)
			throws BussinessException, ContractException, TechnicalException {
		log.info(LogUtil.presentationLogger(EndPointReference.SAVE_SUBSCRIPTION_PLANS + " subscriptionPlansDto "
				+ subscriptionPlansDto.toString()));
		return subscriptionPlansService.saveSubscriptionPlans(subscriptionPlansDto, headers);
	}

	@PostMapping(value = { EndPointReference.FIND_SUBSCRIPTION_PLANS_BY_ID })
	public Map<String, Object> findSubscriptionPlansById(final Long id,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		log.info(LogUtil.presentationLogger(EndPointReference.FIND_SUBSCRIPTION_PLANS_BY_ID + " id " + id));
		return subscriptionPlansService.getSubscriptionPlansById(id, headers);
	}

	@PostMapping(value = { EndPointReference.DELETE_SUBSCRIPTION_PLANS_BY_ID })
	public Map<String, Object> deleteSubscriptionPlansById(final Long id,
			@RequestParam(defaultValue = "false", value = Constants.STATUS, required = false) boolean status,
			@RequestHeader MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException {
		log.info(LogUtil.presentationLogger(
				EndPointReference.DELETE_SUBSCRIPTION_PLANS_BY_ID + " id " + id + ", status" + status));
		return subscriptionPlansService.deleteSubscriptionPlansById(id, status, headers);
	}

	@PostMapping(value = { EndPointReference.GET_ALL_SUBSCRIPTION_PLANS_PAGINATION })
	public Map<String, Object> getAllSubscriptionPlansPagination(
			@RequestBody(required = false) List<SequenceColumnDTO> sequenceColumnDTOs,
			@RequestParam(defaultValue = "0", value = "draw", required = false) int draw,
			@RequestParam(defaultValue = "0", value = "start", required = false) int start,
			@RequestParam(defaultValue = "0", value = "length", required = false) int length,
			@RequestParam(defaultValue = Constants.EMPTYSTRING, value = Constants.COLUMN, required = false) String columns,
			@RequestParam(defaultValue = Constants.EMPTYSTRING, value = Constants.SEARCH, required = false) String search,
			@RequestParam(defaultValue = "desc", value = "sortOrder", required = false) String sortOrder,
			@RequestParam(defaultValue = "subscriptionPlanId", value = "sortField", required = false) String sortField,
			@RequestParam(defaultValue = Constants.EMPTYSTRING, value = "searchCol", required = false) String searchCol,
			@RequestParam(defaultValue = "true", value = Constants.STATUS, required = false) boolean status,
			@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, ContractException, BussinessException {
		log.info(LogUtil.presentationLogger(EndPointReference.GET_ALL_SUBSCRIPTION_PLANS_PAGINATION));
		return subscriptionPlansService.findAllSubscriptionPlansPaginationCriteria1(sequenceColumnDTOs, draw, start,
				length, columns, search, sortOrder, sortField, searchCol, status, headers);
	}

	@PostMapping(value = { EndPointReference.SUBSCRIPTION_PLAN_USER_LIMIT })
	public Map<String, Object> subscriptionPlanUserLimit(@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException,IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.SUBSCRIPTION_PLAN_USER_LIMIT));
		return subscriptionPlansService.subscriptionPlanUserLimit(headers);
	}

	@PostMapping(value = { EndPointReference.GET_SUBSCRIPTION_PLAN_BY_COMPANY_ID })
	public Map<String, Object> getSubscriptionPlanByCompanyId(@RequestHeader MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException {
		log.info(LogUtil.presentationLogger(EndPointReference.GET_SUBSCRIPTION_PLAN_BY_COMPANY_ID));
		return subscriptionPlansService.getSubscriptionPlanByCompanyId(headers);
	}

}
