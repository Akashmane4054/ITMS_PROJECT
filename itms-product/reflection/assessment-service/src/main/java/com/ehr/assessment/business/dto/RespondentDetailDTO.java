package com.ehr.assessment.business.dto;

import java.util.Date;
import java.util.List;

import com.ehr.core.dto.DocumentGetDto;

import lombok.Data;

@Data
public class RespondentDetailDTO {

	private Long userId;
	private Long profileImageId;
	private String title;
	private String firstName;
	private String middleName;
	private String lastName;
	private String gender;
	private String emailAddress;
	private Date dateOfBirth;
	private String mealPreference;

	private Integer weight;
	private Integer weightGrams;
	private Integer heightFt;
	private Integer heightIn;
	private Integer bmiValue;
	private String bloodGroup;
	private String maritalStatus;
	private String spouseName;
	private String spouseGender;
	private Date spouseDateOfBirth;
	private String spouseWork;
	private String spouseBloodGroup;
	private Integer childrenCount;
	private Boolean fatherAlive;
	private String fatherName;
	private String fatherGender;
	private Date fatherDateOfBirth;
	private String fatherWork;
	private String fatherBloodGroup;
	private Boolean motherAlive;
	private String motherName;
	private String motherGender;
	private Date motherDateOfBirth;
	private String motherWork;
	private String motherBloodGroup;
	private Boolean fatherInLawAlive;
	private String fatherInLawName;
	private String fatherInLawGender;
	private Date fatherInLawDateOfBirth;
	private String fatherInLawWork;
	private String fatherInLawBloodGroup;
	private Boolean motherInLawAlive;
	private String motherInLawName;
	private String motherInLawGender;
	private Date motherInLawDateOfBirth;
	private String motherInLawWork;
	private String motherInLawBloodGroup;
	private String child1Plan;
	private String child1Name;
	private String child1Gender;
	private Date child1DateOfBirth;
	private String child1BloodGroup;
	private String child1Work;
	private String child2Plan;
	private String child2Name;
	private String child2Gender;
	private Date child2DateOfBirth;
	private String child2BloodGroup;
	private String child2Work;
	
	
	private Boolean corporateHealthPolicyFlag;
	private String corporateHealthPolicyName;
	private List<CoverageDTO> corporateCoverageDTO;
	private Boolean personalHealthPolicyFlag;
	private String personalHealthPolicyName;
	private List<CoverageDTO> personalCoverageDTO;
	private DocumentGetDto profileImage;

	private Long createdBy;

	private Date createdOn;

	private Long modifiedBy;

	private Date modifiedOn;
}
