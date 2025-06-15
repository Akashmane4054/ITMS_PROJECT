package com.ehr.assessment.integration.domain;

import java.util.Date;

import com.ehr.assessment.business.enums.PolicyType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class RespondentCoverageInfoMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long userId;
	
	private Long coverageId;
	
	private Boolean coverageflag;
	
    @Enumerated(EnumType.STRING)
	private PolicyType policyType;
	
	private Boolean active;

	private Long createdBy;

	private Date createdOn;

}
