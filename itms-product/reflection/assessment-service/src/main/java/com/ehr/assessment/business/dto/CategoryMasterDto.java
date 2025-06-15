package com.ehr.assessment.business.dto;

import lombok.Data;

@Data
public class CategoryMasterDto {

	private Long id;
	private String name;
	private String description;

	private String createdBy;
	private String createdOn;
	private String modifiedBy;
	private String modifiedOn;
	private Boolean active=true;

}
