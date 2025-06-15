package com.ehr.assessment.business.service;

import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;

import com.ehr.assessment.business.dto.CategoryMasterDto;
import com.ehr.core.dto.SequenceColumnDTO;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;

import jakarta.validation.Valid;

public interface CategoryMasterService {
	Map<String, Object> saveCategoryMaster(@Valid CategoryMasterDto categoryMasterDto,
			MultiValueMap<String, String> headers) throws ContractException, TechnicalException, BussinessException;

	Map<String, Object> findCategoryMasterById(Long id, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException;

	Map<String, Object> deleteCategoryMaster(Long id, boolean status, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException;

	Map<String, Object> findAllCategoryMasterPaginationCriteria(List<SequenceColumnDTO> sequenceColumnDTOs, String draw,
			int start, int length, String columns, String search, String sortOrder, String sortField, String searchCol,
			boolean status, MultiValueMap<String, String> headers)
			throws ContractException, TechnicalException, BussinessException;

	Map<String, Object> getAllActiveCategory(MultiValueMap<String, String> headers)
			throws TechnicalException, ContractException, BussinessException;
}
