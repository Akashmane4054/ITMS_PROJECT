package com.ehr.assessment.integration.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(indexes = {@Index(name = "assessment_respondent_demogrpahics_index_1",  columnList="assessmentId"),
		@Index(name = "assessment_respondent_demogrpahics_index_2",  columnList="assessmentId,userId")})
public class AssessmentRespondentDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	private Long assessmentId;

}
