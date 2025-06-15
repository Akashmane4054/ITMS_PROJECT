package com.ehr.assessment.business.dto;

import java.util.Date;
import java.util.List;

import com.ehr.assessment.business.enums.AssessmentStatus;

import lombok.Data;

@Data
public class AssessmentInsightsDetailsDTO {
	
	private Long id;
	private String name;
	private String description;
	private Long companyId;
	private String assessmentDisplayId;
	private Long templateId;
	private String financialYear;
	private Date expiredOn;
	private Long owner;
	private String ownerName;
	private Boolean selfReview;
	private Long reviewer;
	private Date reviewedOn;
	private Long scheduledBy;
	private Date scheduledOn;
	private AssessmentStatus status;
	private List<SectionDto> sections;

}
