package com.ehr.assessment.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalProjectionDTO {
	private Long userId;
	private String fullName;
	private String emailAddress;
}
