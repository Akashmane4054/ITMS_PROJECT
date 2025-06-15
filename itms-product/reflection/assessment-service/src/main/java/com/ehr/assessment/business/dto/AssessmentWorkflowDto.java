package com.ehr.assessment.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssessmentWorkflowDto {

	private Long id;
	private Long assessmentId;
	private Integer status;
	private String createdOn;
	private Long createdBy;
	
	private String modifiedOn;
	private Long modifiedBy;
	
	private String createdByName;
	private String modifiedByName;
}
