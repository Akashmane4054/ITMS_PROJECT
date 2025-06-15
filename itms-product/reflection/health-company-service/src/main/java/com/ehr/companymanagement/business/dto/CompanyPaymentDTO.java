package com.ehr.companymanagement.business.dto;

import java.util.Date;

import lombok.Data;

@Data
public class CompanyPaymentDTO {

	private Long id;

	private Long companyId;

	private Long subscriptionPlanId;

	private Double paymentAmount;

	private Date paymentOn;

	private Long expiryDate;

	private Long nextPaymentDate;
	
}
