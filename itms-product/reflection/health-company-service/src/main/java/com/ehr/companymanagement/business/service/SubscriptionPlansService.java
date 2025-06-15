
package com.ehr.companymanagement.business.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;

import com.ehr.companymanagement.business.dto.SubscriptionPlansDto;
import com.ehr.core.dto.SequenceColumnDTO;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;

public interface SubscriptionPlansService {
	public Map<String, Object> getSubscriptionPlansById(Long subscriptionPlanId, MultiValueMap<String, String> headers)
			throws ContractException, BussinessException, TechnicalException;

	public Map<String, Object> saveSubscriptionPlans(SubscriptionPlansDto subcriptionPlansDTO,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException;

	public Map<String, Object> deleteSubscriptionPlansById(Long subscriptionPlanId, boolean status,
			MultiValueMap<String, String> headers) throws ContractException, BussinessException, TechnicalException;

	public Map<String, Object> findAllSubscriptionPlansPaginationCriteria1(List<SequenceColumnDTO> sequenceColumnDTOs,
			int draw, int start, int length, String columns, String search, String sortOrder, String sortField,
			String searchCol, boolean booleanfield, MultiValueMap<String, String> headers)
			throws ContractException, BussinessException, TechnicalException;

	public Map<String, Object> subscriptionPlanUserLimit(MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException;

	public Map<String, Object> getSubscriptionPlanByCompanyId(MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException, IOException;

}
