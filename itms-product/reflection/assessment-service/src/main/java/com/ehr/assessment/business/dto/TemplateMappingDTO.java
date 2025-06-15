package com.ehr.assessment.business.dto;

import com.ehr.assessment.business.enums.AssessmentProgress;

import lombok.Data;

@Data
public class TemplateMappingDTO {
	
	private Long assessmentId;
	
	private Long templateId;
	
	private AssessmentProgress progressStatus;

}
