package com.ehr.assessment.business.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespondentProjectionDTO {

	private Long userId;
	private String emailAddress;
	private Date dateOfBirth;
	private String gender;
	
}
