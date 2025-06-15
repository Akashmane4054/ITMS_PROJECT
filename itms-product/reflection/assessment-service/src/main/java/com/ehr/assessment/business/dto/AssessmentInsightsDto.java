package com.ehr.assessment.business.dto;

import lombok.Data;

@Data
public class AssessmentInsightsDto {

	private Integer noOfRespondent;
	private Integer noOfResponseSubmitted;
	private Integer noOfResponseNotSubmitted;
	private Integer noOfResponseSubmittedPercent;
}
