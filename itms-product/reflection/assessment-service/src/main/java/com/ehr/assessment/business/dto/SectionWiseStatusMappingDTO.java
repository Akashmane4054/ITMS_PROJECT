package com.ehr.assessment.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectionWiseStatusMappingDTO {

	private Long id;

	private String sectionResponseStatus;

	private Long sequence;

	private Long sectionId;

	private Long assessmentId;

	private Long userId;

	private Integer totalQuestionInOneSection;

}
