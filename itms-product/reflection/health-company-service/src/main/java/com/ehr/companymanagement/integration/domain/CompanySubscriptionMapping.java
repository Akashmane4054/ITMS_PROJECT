package com.ehr.companymanagement.integration.domain;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * @author SwapniL
 */
@Entity
@Data
public class CompanySubscriptionMapping implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long serialId;

	private Long companyId;

	private Long subscriptionPlanId;

	private Date validFrom;

	private Date validTo;

	private Integer noOfAssessments;

	private String noOfUsers;

	private Boolean active;

	private Long createdBy;

	private Date createdOn;

	private Long modifiedBy;

	private Date modifiedOn;

}
