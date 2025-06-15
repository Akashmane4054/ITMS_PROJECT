package com.ehr.assessment.business.dto;

import java.util.List;

import lombok.Data;

@Data
public class PolicyDTO {
	
	private Boolean corporateHealthPolicyFlag;
	private String corporateHealthPolicyName;
	private List<CoverageDTO> corporateCoverageDTO;
	private Boolean personalHealthPolicyFlag;
	private String personalHealthPolicyName;
	private List<CoverageDTO> personalCoverageDTO;
	
}
