package com.ehr.assessment.business.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class RespondentUserProfileDetailsDTOForAISuggestion {

	private Date dateOfBirth;
	private String gender;
	private String maritalStatus;
	private Integer heightFt;
	private Integer heightIn;
	private Integer weight;
	private Integer weightGrams;
	private String bloodGroup;
	private String mealPreference;
	private Boolean motherAlive;
	private String motherName;
	private Date motherDateOfBirth;
	private String motherGender;
	private String motherWork;
	private String motherBloodGroup;
	private Boolean fatherAlive;
	private String fatherName;
	private Date fatherDateOfBirth;
	private String fatherGender;
	private String fatherWork;
	private String fatherBloodGroup;
	private Boolean corporateHealthPolicyFlag;
	private String corporateHealthPolicyName;
	private List<String> corporateHealthCoverageTo;
	private Boolean personalHealthPolicyFlag;
	private String personalHealthPolicyName;
	private List<String> personalHealthCoverageTo;
}
