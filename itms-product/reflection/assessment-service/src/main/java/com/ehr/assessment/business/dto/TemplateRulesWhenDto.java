package com.ehr.assessment.business.dto;

import java.io.Serializable;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity

public class TemplateRulesWhenDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private Long templateRuleId;

	private Long sectionId;
	
	private Long templateId;

	private Long actionOn;

	private Boolean equality;

	private String elementValue;

	private Integer conditionalType;
}
