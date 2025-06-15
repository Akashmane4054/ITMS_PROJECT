package com.ehr.assessment.business.dto;

import lombok.Data;

@Data
public class GenderResponseDTO {

	private GenderDetailsDTO male;
	private GenderDetailsDTO female;
	private GenderDetailsDTO total;

	public GenderResponseDTO(int maleNoOfMembers, double malePercentage, int femaleNoOfMembers,
			double femalePercentage) {
		this.male = new GenderDetailsDTO(maleNoOfMembers, malePercentage);
		this.female = new GenderDetailsDTO(femaleNoOfMembers, femalePercentage);
		int totalNoOfMembers = maleNoOfMembers + femaleNoOfMembers;
		double totalPercentage = 100.0;
		this.total = new GenderDetailsDTO(totalNoOfMembers, totalPercentage);
	}
}
