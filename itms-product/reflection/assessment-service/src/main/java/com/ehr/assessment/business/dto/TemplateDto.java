package com.ehr.assessment.business.dto;

import java.util.List;

import com.ehr.assessment.business.enums.TemplateStatus;

import lombok.Data;

@Data
public class TemplateDto {

	private Long id;
	private String name;
	private String description;

	private Integer templateType;
	private Long templateId;

	private Integer version;
	private Integer versionNumber;

	private TemplateStatus status;

	private Boolean selfReview;
	private String reviewer;
	private String reviewedOn;
	private List<Long> categoryIds;
	private List<Long> domainIds;
	private List<CategoryMasterDto> categories;
	private List<DomainMasterDto> domains;
	private List<SectionDto> sections;
	private List<TemplateRulesDto> rules;

	private String createdBy;
	private String createdOn;
	private String modifiedBy;
	private String modifiedOn;
	private Boolean active = true;

}
