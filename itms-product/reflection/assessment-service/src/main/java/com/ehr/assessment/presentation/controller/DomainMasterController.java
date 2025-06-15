package com.ehr.assessment.presentation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ehr.assessment.business.dto.DomainMasterDto;
import com.ehr.assessment.business.service.DomainMasterService;
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
public class DomainMasterController {

    
    private final DomainMasterService domainMasterService;

    @PostMapping(EndPointReference.SAVE_DOMAIN_MASTER)
    public Map<String, Object> saveDomainMaster(@RequestBody @Valid DomainMasterDto domainMasterDto,
                                                @RequestHeader MultiValueMap<String, String> headers)
            throws BussinessException, TechnicalException, ContractException {
        log.info(LogUtil
                .presentationLogger(EndPointReference.SAVE_DOMAIN_MASTER + " domainMasterDto " + domainMasterDto.toString()));
        return domainMasterService.saveDomainMaster(domainMasterDto, headers);
    }

    @PostMapping(EndPointReference.FIND_DOMAIN_MASTER_BY_ID)
    public Map<String, Object> findDomainMasterById(@RequestParam("id") Long id,
                                                    @RequestHeader MultiValueMap<String, String> headers)
            throws ContractException, BussinessException, TechnicalException {
        log.info(LogUtil.presentationLogger(EndPointReference.FIND_DOMAIN_MASTER_BY_ID + " domainMasterId " + id));
        return domainMasterService.findDomainMasterById(id, headers);
    }

    @PostMapping(EndPointReference.DELETE_DOMAIN_MASTER)
    public Map<String, Object> deleteDomainMaster(@RequestParam("id") Long id,
                                                  @RequestParam(defaultValue = "false", value = Constants.STATUS, required = false) boolean status,
                                                  @RequestHeader MultiValueMap<String, String> headers)
            throws BussinessException, TechnicalException, ContractException {
        log.info(LogUtil.presentationLogger(
                EndPointReference.DELETE_DOMAIN_MASTER + " domainMaster " + id + ", status " + status));
        return domainMasterService.deleteDomainMaster(id, status, headers);
    }

    @PostMapping(value = {EndPointReference.GET_ALL_DOMAIN_MASTER_PAGINATION})
    public Map<String, Object> getAllDomainMasterPaginationCriteria(
            @RequestBody(required = false) List<SequenceColumnDTO> sequenceColumnDTOs,
            @RequestParam(defaultValue = "0", value = "draw", required = false) String drawStr,
            @RequestParam(defaultValue = "0", value = "start", required = false) int start,
            @RequestParam(defaultValue = "0", value = "length", required = false) int length,
            @RequestParam(defaultValue = Constants.EMPTYSTRING, value = Constants.COLUMN, required = false) String columns,
            @RequestParam(defaultValue = Constants.EMPTYSTRING, value = Constants.SEARCH, required = false) String search,
            @RequestParam(defaultValue = "desc", value = "sortOrder", required = false) String sortOrder,
            @RequestParam(defaultValue = "domainId", value = "sortField", required = false) String sortField,
            @RequestParam(defaultValue = Constants.EMPTYSTRING, value = "searchCol", required = false) String searchCol,
            @RequestParam(defaultValue = "true", value = Constants.STATUS, required = false) boolean status,
            @RequestHeader MultiValueMap<String, String> headers)
            throws TechnicalException, ContractException, BussinessException {
        log.info(LogUtil.presentationLogger(EndPointReference.GET_ALL_DOMAIN_MASTER_PAGINATION));
        return domainMasterService.findAllDomainMasterPaginationCriteria(sequenceColumnDTOs, drawStr, start,
                length, columns, search, sortOrder, sortField, searchCol, status, headers);
    }

    @PostMapping(EndPointReference.GET_ALL_ACTIVE_DOMAIN)
    public Map<String, Object> getAllActivedDomain(@RequestHeader MultiValueMap<String, String> headers)
            throws TechnicalException, ContractException, BussinessException {
        log.info(LogUtil.presentationLogger(EndPointReference.GET_ALL_ACTIVE_DOMAIN));
        return domainMasterService.getAllActivedDomain(headers);
    }

}
