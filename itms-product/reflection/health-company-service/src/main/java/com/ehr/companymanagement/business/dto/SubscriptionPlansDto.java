package com.ehr.companymanagement.business.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionPlansDto {

	private Long subscriptionPlanId;

	@NotNull(message = "PN001")
	@NotEmpty(message = "PN002")
	private String planName;

	private String planDescription;

	private String createdBy;

	private String createdOn;

	private String modifiedBy;

	private String modifiedOn;

	private Long numOfCompanys;

	private Long type;

	private Long numOfUsers;

	private List<String> companys;

	private String typeName;

}
