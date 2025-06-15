package com.ehr.assessment.business.service;

import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;

import com.ehr.assessment.business.dto.DomainMasterDto;
import com.ehr.core.dto.SequenceColumnDTO;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;

import jakarta.validation.Valid;

public interface DomainMasterService {
	Map<String, Object> saveDomainMaster(@Valid DomainMasterDto domainMasterDto, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException;

	Map<String, Object> findDomainMasterById(Long id, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException;

	Map<String, Object> deleteDomainMaster(Long id, boolean status, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException;

	Map<String, Object> findAllDomainMasterPaginationCriteria(List<SequenceColumnDTO> sequenceColumnDTOs, String drawStr,
			int start, int length, String columns, String search, String sortOrder, String sortField, String searchCol,
			boolean status, MultiValueMap<String, String> headers)
			throws TechnicalException, ContractException, BussinessException;

	Map<String, Object> getAllActivedDomain(MultiValueMap<String, String> headers)
			throws TechnicalException, ContractException, BussinessException;
}
