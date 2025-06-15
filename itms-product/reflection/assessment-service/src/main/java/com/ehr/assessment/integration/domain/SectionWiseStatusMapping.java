package com.ehr.assessment.integration.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class SectionWiseStatusMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String sectionResponseStatus;

	private Long sequence;

	private Long sectionId;

	private Long assessmentId;

	private Long userId;

	private Integer totalQuestionInOneSection;

	private Integer responseCountInOneSection;

}
