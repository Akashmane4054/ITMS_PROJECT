package com.ehr.companymanagement.integration.domain;

import java.util.Date;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class CommisionDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long companyId;
	
	private Long insurerId;
	
	private Long insuranceProductId;
	
	private Integer commisionRate;
	
	private Date startDate;
	private Date endDate;	
	
	
}
