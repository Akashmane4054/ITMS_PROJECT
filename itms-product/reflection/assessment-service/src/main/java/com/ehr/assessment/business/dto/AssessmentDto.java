package com.ehr.assessment.business.dto;

import java.util.Date;
import java.util.List;

import com.ehr.assessment.business.enums.AssessmentProgress;
import com.ehr.assessment.business.enums.AssessmentStatus;
import com.ehr.core.dto.UserMasterDTO;

import lombok.Data;

@Data
public class AssessmentDto {

	private Long id;
	private String name;
	private String description;
	private Long companyId;

	private String assessmentDisplayId;
	private Long assessmentDisplayIdSequence;

	private Long templateId;
	private String templateName;
	private Integer templateType;

	private String financialYear;
	private String expiredOn;
	private Long owner;
	private String ownerName;

	private List<Long> userIds;
	private List<UserMasterDTO> users;

	private List<SectionDto> sections;

	private List<Long> submittedUserId;
	private List<SubMittedUserDto> subMittedUserDto;
	private Boolean submittedResponseFlag;
	private Date submittedAssessmentResponseOn;

	private Boolean selfReview;
	private Long reviewer;
	private String reviewedOn;
	private String reviewerName;

	private Long scheduledBy;
	private String scheduledOn;
	private String scheduledByName;
	private Integer status;
	private AssessmentStatus statusValue;
	private String assessmentResponseStatus;

	private String language;

	private String createdBy;
	private String createdOn;
	private String modifiedBy;
	private String modifiedOn;
	private Boolean active = true;

	private Boolean isAssessmentPlay;

	private AssessmentProgress progressStatus;
	private List<AssessmentWorkflowDto> workflowDto;

	private List<AssessmentUserMappingDTO> userMappingDTOs;

	private AssessmentInsightsDto assessmentInsightsDto;

	private Boolean sentOnApproval;

	private Integer totalQuestionInOneAssessment;
	private Integer totalResponseCountInOneAssessment;
	private Double userResponsePercent;
	private Double averageScoreInOneAssessment;

	private List<TemplateRulesDto> rules;

	private List<CategoryMasterDto> categories;

}
