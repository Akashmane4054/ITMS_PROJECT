package com.ehr.assessment.business.dto;

import java.util.Date;

import com.ehr.core.dto.DocumentGetDto;

import lombok.Data;

@Data
public class RespondentDetailsDTO {

	private Long userId;
	private Long profileImageId;
	private AboutMeDTO aboutMe;
	private FamilyDTO familyDTO;
	private PolicyDTO policyDTO;
	private DocumentGetDto profileImage;

	private Long createdBy;
	private Date createdOn;
	private Long modifiedBy;
	private Date modifiedOn;

}
