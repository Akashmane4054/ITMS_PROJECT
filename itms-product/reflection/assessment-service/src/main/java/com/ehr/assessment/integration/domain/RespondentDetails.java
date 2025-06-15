package com.ehr.assessment.integration.domain;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RespondentDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;
	private Long profileImageId;
	private String title;
	private String firstName;
	private String middleName;
	private String lastName;
	private String gender;
	private String emailAddress;
	private Date dateOfBirth;

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
	private Boolean corporateHealthPolicyFlag;
	private String corporateHealthPolicyName;
	private Boolean personalHealthPolicyFlag;
	private String personalHealthPolicyName;
	private String mealPreference;
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

	private Long createdBy;

	private Date createdOn;

	private Long modifiedBy;

	private Date modifiedOn;

}
