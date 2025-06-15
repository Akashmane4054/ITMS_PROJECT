package com.ehr.assessment.integration.domain;

import java.util.Date;

import com.ehr.assessment.business.enums.AssessmentProgress;
import com.ehr.assessment.business.enums.AssessmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 *  Assessment are stored in this table 
 *  
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = { 
	    @Index(name = "assessment_index_1", columnList = "companyId,status"),
	    @Index(name = "assessment_index_2", columnList = "companyId"),
	    @Index(name = "assessment_index_3", columnList = "companyId,active"),
	    @Index(name = "assessment_index_4", columnList = "companyId,financialYear"),
	    @Index(name = "assessment_index_5", columnList = "id,companyId,active") 
	})
public class Assessment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(length = 1000)
	private String description;

	private Long companyId;

	private String assessmentDisplayId;

	private Long assessmentDisplayIdSequence;

	private Long templateId;

	private String financialYear;

	private Date expiredOn;
	
	@NotNull
    @Enumerated(EnumType.STRING)
	private AssessmentStatus status;

	private Boolean selfReview;

	private Long reviewer;

	private Date reviewedOn;

	private Long owner;

	private Long scheduledBy;

	private Date scheduledOn;

	private Long createdBy;

	private Date createdOn;
	
	private Date publishedOn;

	private Long modifiedBy;

	private Date modifiedOn;
	
	private Long languageId;
	
	@NotNull
    @Enumerated(EnumType.STRING)
	private AssessmentProgress progressStatus;

	private Boolean active = true;
	
	private Boolean isAssessmentPlay;
	
	private Boolean sentOnApproval;

}
