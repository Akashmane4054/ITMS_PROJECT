package com.ehr.assessment.business.dto;

import lombok.Data;

@Data
public class GenderDetailsDTO {
	private int noOfMembers;
	private double percentage;

	public GenderDetailsDTO(int noOfMembers, double percentage) {
		this.noOfMembers = noOfMembers;
		this.percentage = percentage;
	}
}
