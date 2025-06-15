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

public class TemplateRulesWhen implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uuid;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long templateRuleId;

	private Long sectionId;

	private Long actionOn;

	private Boolean equality;

	private String elementValue;

	private Integer conditionalType;

	private Long templateId;

	private Boolean active = true;
	private Long modifiedBy;
	private Date createdOn;
	private Long createdBy;
	private Date modifiedOn;
}
