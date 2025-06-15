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
public class CompanyDetails implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 500)
	private String companyName;

	private Long addressId;

	private String stdCode;

	private String landlineNo;

	private Long logoId;

	private Long emailLogoId;

	private Long timeZone;

	private Long currency;

	private Long language;

	private Long subId;

	private Long adminId;

	private Long createdBy;

	private Date createdOn;

	private Long modifiedBy;

	private Date modifiedOn;

	private Boolean active = true;
	
	// used for listing purpose
	private String emailAddress;
	private String planName;
	private String fullName;
	private Date validFrom;
	private Date validTo;

}
