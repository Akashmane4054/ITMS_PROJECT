package com.ehr.assessment.integration.domain;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class AssessmentWorkFlow {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long assessmentId;
	private Integer actionType;
	private Integer status;
	private Date createdOn;
	private Long createdBy;
	
	private Date modifiedOn;
	private Long modifiedBy;
	
}
