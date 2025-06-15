package com.ehr.assessment.business.dto;

import java.util.Date;

import lombok.Data;

@Data
public class AboutMeDTO {
	
	private String firstName;
	private String lastName;
	private String gender;
	private String emailAddress;
	private Date dateOfBirth;
	private Integer weight;
	private Integer weightGrams;
	private Integer heightFt;
	private Integer heightIn;
	private Double bmiValue;
	private String bloodGroup;
	private String maritalStatus;
	private String mealPreference;

}
