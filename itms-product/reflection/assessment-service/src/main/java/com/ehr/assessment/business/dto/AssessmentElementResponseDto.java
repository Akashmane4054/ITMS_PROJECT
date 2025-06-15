package com.ehr.assessment.business.dto;

import java.util.List;

import com.ehr.assessment.business.enums.AssessmentResponseType;
import com.ehr.core.dto.DocumentGetDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentElementResponseDto {

	private Long id;
	private Long assessmentId;
	private Long sectionId;
	private Long elementId;

	private List<Double> assessmentWeightage;
	private AssessmentResponseType assessmentResponseType;
	private String comments;

	private String createdBy;
	private String createdOn;
	private String modifiedBy;
	private String modifiedOn;

	private List<DocumentGetDto> documentDetailsDto;
	private List<DocumentGetDto> commentsDocumentDto;
	private List<String> responses;

	private List<String> spouseResponse;
	private List<String> motherResponse;
	private List<String> fatherResponse;
	private List<String> motherinlawResponse;
	private List<String> fatherinlawResponse;
	private List<String> child1Response;
	private List<String> child2Response;

	private Boolean isQueryFlag;
	private String queryDiscrption;
	private Long queryId;

}
