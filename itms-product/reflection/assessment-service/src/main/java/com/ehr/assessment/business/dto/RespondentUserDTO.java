package com.ehr.assessment.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespondentUserDTO {

	private Long userId;
	private String firstName;
	private String lastName;
	private String fullName;
	private String emailAddress;
	private String createdOn;
	private Long createdBy;
	private String createdByName;
	private Long assessmentId;
	private String assessmentResponseStatus;

}
