package com.ehr.assessment.presentation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ehr.assessment.business.dto.CategoryMasterDto;
import com.ehr.assessment.business.service.CategoryMasterService;
import com.ehr.core.dto.SequenceColumnDTO;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.util.Constants;
import com.ehr.core.util.EndPointReference;
import com.ehr.core.util.LogUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CategoryMasterController {

    private final CategoryMasterService categoryMasterService;

    @PostMapping(EndPointReference.SAVE_CATEGORY_MASTER)
    public Map<String, Object> saveCategoryMaster(@RequestBody @Valid CategoryMasterDto categoryMasterDto,
                                                  @RequestHeader MultiValueMap<String, String> headers)
            throws BussinessException, TechnicalException, ContractException {
        log.info(LogUtil
                .presentationLogger(EndPointReference.SAVE_CATEGORY_MASTER + " categoryMasterDto " + categoryMasterDto.toString()));
        return categoryMasterService.saveCategoryMaster(categoryMasterDto, headers);
    }

    @PostMapping(EndPointReference.FIND_CATEGORY_MASTER_BY_ID)
    public Map<String, Object> findCategoryMasterById(@RequestParam("id") Long id,
                                                      @RequestHeader MultiValueMap<String, String> headers)
            throws ContractException, BussinessException, TechnicalException {
        log.info(LogUtil.presentationLogger(EndPointReference.FIND_CATEGORY_MASTER_BY_ID + " categoryMasterId " + id));
        return categoryMasterService.findCategoryMasterById(id, headers);
    }

    @PostMapping(EndPointReference.DELETE_CATEGORY_MASTER)
    public Map<String, Object> deleteCategoryMaster(@RequestParam("id") Long id,
                                                    @RequestParam(defaultValue = "false", value = Constants.STATUS, required = false) boolean status,
                                                    @RequestHeader MultiValueMap<String, String> headers)
            throws BussinessException, TechnicalException, ContractException {
        log.info(LogUtil.presentationLogger(
                EndPointReference.DELETE_CATEGORY_MASTER + " categoryMaster " + id + ", status " + status));
        return categoryMasterService.deleteCategoryMaster(id, status, headers);
    }

    @PostMapping(value = { EndPointReference.GET_ALL_CATEGORY_MASTER_PAGINATION })
    public Map<String, Object> getAllCategoryMasterPaginationCriteria(
            @RequestBody(required = false) List<SequenceColumnDTO> sequenceColumnDTOs,
            @RequestParam(defaultValue = "0", value = "draw", required = false) String draw,
            @RequestParam(defaultValue = "0", value = "start", required = false) int start,
            @RequestParam(defaultValue = "0", value = "length", required = false) int length,
            @RequestParam(defaultValue = Constants.EMPTYSTRING, value = Constants.COLUMN, required = false) String columns,
            @RequestParam(defaultValue = Constants.EMPTYSTRING, value = Constants.SEARCH, required = false) String search,
            @RequestParam(defaultValue = "desc", value = "sortOrder", required = false) String sortOrder,
            @RequestParam(defaultValue = "id", value = "sortField", required = false) String sortField,
            @RequestParam(defaultValue = Constants.EMPTYSTRING, value = "searchCol", required = false) String searchCol,
            @RequestParam(defaultValue = "true", value = Constants.STATUS, required = false) boolean status,
            @RequestHeader MultiValueMap<String, String> headers)
            throws TechnicalException, ContractException, BussinessException {
        log.info(LogUtil.presentationLogger(EndPointReference.GET_ALL_CATEGORY_MASTER_PAGINATION));
        return categoryMasterService.findAllCategoryMasterPaginationCriteria(sequenceColumnDTOs, draw, start,
                length, columns, search, sortOrder, sortField, searchCol, status, headers);
    }

    @PostMapping(EndPointReference.GET_ALL_ACTIVE_CATEGORY)
    public Map<String, Object> getAllActiveCategory(@RequestHeader MultiValueMap<String, String> headers)
            throws TechnicalException, ContractException, BussinessException {
        log.info(LogUtil.presentationLogger(EndPointReference.GET_ALL_ACTIVE_CATEGORY));
        return categoryMasterService.getAllActiveCategory(headers);
    }

}
