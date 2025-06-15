package com.ehr.companymanagement.business.dto;


import com.ehr.core.dto.AddressDTO;
import com.ehr.core.dto.DocumentGetDto;
import com.ehr.core.dto.UserMasterDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyDetailsDTO {

	@NotNull
	UserMasterDTO adminUser;
	private Long id;
	private String serialId;
	@NotEmpty(message = "CN001")
	@NotNull(message = "CN002")
	private String companyName;

	private String website;
	@NotEmpty(message = "STD001")
	@NotNull(message = "STD002")
	private String stdCode;
	@NotEmpty(message = "LL001")
	@NotNull(message = "LL002")
	private String landlineNo;
	private DocumentGetDto logo;
	private DocumentGetDto emailLogo;
	@NotNull(message = "TZ001")
	private Long timeZone;
	@NotNull(message = "CR001")
	private Long currency;
	@NotNull(message = "LANG001")
	private Long language;
	@NotNull(message = "SUB001")
	private Long subId;
	private SubscriptionPlansDto subscriptionPlansDto;
	private Long validFrom;
	private Long validTo;
	private Long adminId;

	private String createdOn;

	private String createdBy;

	private String modifiedOn;

	private String modifiedBy;

	private String effectiveFromDate;

	private String effectiveToDate;

	private String noOfUsers;
	
	private Integer noOfAssessments;
	
	private CompanyPaymentDTO paymentDto;
	
	private  AddressDTO addressDto;

}
