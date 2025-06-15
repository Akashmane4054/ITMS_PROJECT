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
public class CompanyPaymentDetails implements Serializable {


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long companyId;

    private Long subscriptionPlanId;

    private Double paymentAmount;

    private Date paymentOn;

    private Date expiryDate;
    
    private Date nextPaymentDate;

    private Long createdBy;

    private Date createdOn;

    private Long modifiedBy;

    private Date modifiedOn;
    
    private Boolean active = Boolean.TRUE;

}
