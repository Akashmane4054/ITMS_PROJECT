package com.ehr.assessment.integration.domain;

import java.util.Date;

import com.ehr.assessment.business.enums.AssessmentResponseType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Here we store the answers selected/written by respondents 
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(indexes = {
		@Index(name = "assessment_element_response_index_1", columnList = "createdBy,elementId,assessmentId") })
public class AssessmentElementResponse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long assessmentId;

	private Long elementId;

	private Long sectionId;

	@Column(length = 1500)
	private String response;

	private String spouseResponse;
	private String motherResponse;
	private String fatherResponse;
	private String motherinlawResponse;
	private String fatherinlawResponse;
	private String child1Response;
	private String child2Response;

	private String assessmentWeightage;

	@Enumerated(EnumType.STRING)
	private AssessmentResponseType assessmentResponseType;

	private String comments;
	private String documentId;

	private Long createdBy;

	private Date createdOn;

	private Long modifiedBy;

	private Date modifiedOn;

}
