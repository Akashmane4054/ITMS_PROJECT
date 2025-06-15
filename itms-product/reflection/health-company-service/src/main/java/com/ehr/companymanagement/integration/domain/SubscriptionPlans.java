/**
 *
 */
package com.ehr.companymanagement.integration.domain;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
public class SubscriptionPlans implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long subscriptionPlanId;

	private String serialId;

	@Column(length = 100)
	private String planName;

	@Column(length = 1000)
	private String planDescription;

	private Long type;

	private Boolean active;

	private Long createdBy;

	private Date createdOn;

	private Long modifiedBy;

	private Date modifiedOn;

	private Long companyId;
	
	private Long numOfUsers;

}
