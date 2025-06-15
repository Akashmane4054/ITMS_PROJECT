package com.ehr.assessment.integration.domain;

import java.util.Date;

import com.ehr.assessment.business.enums.AssessmentRemainder;
import com.ehr.assessment.business.enums.AssessmentResponseStatus;

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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(indexes = { @Index(name = "assessment_user_index_1", columnList = "assessmentId") })
public class AssessmentUserMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;
	private Long assessmentId;

	@Enumerated(EnumType.STRING)
	private AssessmentResponseStatus assessmentResponseStatus;
	
	private Date submittedOn;
	
	@Enumerated(EnumType.STRING)
	private AssessmentRemainder emailReminder;
	
	private Date lastReminderSent;

	private Long createdBy;
	private Date createdOn;
	private Long modifiedBy;
	private Date modifiedOn;
	private Boolean active = true;

}
