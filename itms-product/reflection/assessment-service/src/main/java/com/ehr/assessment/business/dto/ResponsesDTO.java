package com.ehr.assessment.business.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponsesDTO {

	private List<AssessmentElementResponseDto> elementResponseDtos;

	private Long totalQuestionInOneAssessment;

	private Long totalResponseCountInOneAssessment;

}
