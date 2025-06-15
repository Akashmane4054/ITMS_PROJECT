package com.ehr.companymanagement.business.dto;

import java.util.List;

import lombok.Data;

@Data
public class CompanyDetailsPaginationDTO {

	private List<SequenceColumnDTO> sequenceColumnDTOs;
	private int draw = 0;
	private int start = 0;
	private int length = 0;
	private String columns = "";
	private String search = "";
	private String sortOrder = "desc";
	private String sortField = "id";
	private String searchCol = "";
	private Boolean status ;
	private Boolean isEffective;

}
