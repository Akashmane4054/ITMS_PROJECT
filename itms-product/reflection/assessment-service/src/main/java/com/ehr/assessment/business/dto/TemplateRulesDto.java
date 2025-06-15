package com.ehr.assessment.business.dto;

import java.util.List;

import lombok.Data;

@Data
public class TemplateRulesDto {

	private Long id;

	private String name;

	private String description;

	private Long templateId;

	private Boolean active = true;

	private List<TemplateRulesWhenDto> when;

	private List<TemplateRulesThenDto> then;

	private String modifiedBy;
	private String createdOn;
	private String createdBy;
	private String modifiedOn;
}
