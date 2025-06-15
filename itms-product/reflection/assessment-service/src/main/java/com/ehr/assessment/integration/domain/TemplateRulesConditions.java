package com.ehr.assessment.integration.domain;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity

public class TemplateRulesConditions implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uuid;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long templateRuleId;

	private Long elementId;

	private Long sectionId;

	private Long templateId;

	private Integer conditionType;

	private Integer conditionElementOptionLabel;

	private Long impactElementId;

	private Long impactSectionId;

	private Boolean impactElementDisplayFlag;
	private Boolean impactElementMandatoryFlag;

	private Boolean active = true;
	private Long modifiedBy;
	private Date createdOn;
	private Long createdBy;
	private Date modifiedOn;
}
