package com.ehr.assessment.business.dto;

import java.util.Date;

import lombok.Data;

@Data
public class FamilyDTO {
	
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
	private String spouseName;
	private String spouseGender;
	private String spouseWork;
	private String spouseBloodGroup;
    private Date spouseDateOfBirth; 
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

}
