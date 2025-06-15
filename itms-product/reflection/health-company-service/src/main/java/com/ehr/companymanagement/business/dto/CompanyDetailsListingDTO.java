package com.ehr.companymanagement.business.dto;

import java.util.List;

import com.ehr.core.dto.DocumentGetDto;
import com.ehr.core.dto.UserMasterDTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyDetailsListingDTO {

	@NotNull
	UserMasterDTO adminUser;
	private Long id;
	private String serialId;
	@NotEmpty(message = "CN001")
	@NotNull(message = "CN002")
	private String companyName;
	@NotEmpty(message = "AL001")
	@NotNull(message = "AL002")
	private String addressLine1;
	private String addressLine2;
	private String pincode;
	@NotNull(message = "C001")
	private Long cityId;
	private Long stateId;
	private Long countryId;
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
	
	private List<CompanyPaymentDTO> paymentDto;
	
}
