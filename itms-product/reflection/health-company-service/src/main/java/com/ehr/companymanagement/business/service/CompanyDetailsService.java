package com.ehr.companymanagement.business.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;

import com.ehr.companymanagement.business.dto.CompanyDetailsDTO;
import com.ehr.core.dto.SequenceColumnDTO;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;

public interface CompanyDetailsService {

	Map<String, Object> saveCompanyDetails(CompanyDetailsDTO companyDto, MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException, IOException;

	Map<String, Object> findCompanyDetailsById(Long id, MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException, IOException;

	Map<String, Object> findAllCompanyDetailsByPaginationCriteria(List<SequenceColumnDTO> sequenceColumnDTOs, int draw,
			int start, int length, String columns, String search, String sortOrder, String sortField, String searchCol,
			boolean booleanfield, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> getCompanySubscriptionPlainValidity(MultiValueMap<String, String> headers)
			throws TechnicalException, ContractException, BussinessException;

	Map<String, Object> getLogoByCompanyId(Long id)
			throws TechnicalException, ContractException, BussinessException, IOException;

	Map<String, Object> getAllCompanyDetailsPaginationByEffectiveFromAndTill(List<SequenceColumnDTO> sequenceColumnDTOs,
			int draw, int start, int length, String columns, String search, String sortOrder, String sortField,
			String searchCol, boolean isEffective, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> getCompanySubscriptionPlanStatus(Long id) throws TechnicalException;

	Map<String, Object> findCountryIdByCompanyId(Long companyId, MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException, IOException;

	Map<String, Object> validateCompanyId(Long companyId, MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException;

	Map<String, Object> getCompanyNameByCompanyId(Long companyId)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> deleteCompanyById(Long id, boolean status, MultiValueMap<String, String> headers)
			throws TechnicalException, BussinessException, ContractException;

	Map<String, Object> findCompanyNameById(Long id, MultiValueMap<String, String> headers)
			throws BussinessException, ContractException, TechnicalException;

}
