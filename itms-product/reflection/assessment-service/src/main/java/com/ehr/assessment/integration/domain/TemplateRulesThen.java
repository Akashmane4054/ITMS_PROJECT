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

public class TemplateRulesThen implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uuid;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long templateRuleId;

	private Long sectionId;

	private Long impactOn;

	private Boolean display;

	private Boolean required;

	private Boolean active = true;

	private Long templateId;

	private Long modifiedBy;
	private Date createdOn;
	private Long createdBy;
	private Date modifiedOn;
}
