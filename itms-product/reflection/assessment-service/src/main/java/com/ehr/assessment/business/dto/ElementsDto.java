package com.ehr.assessment.business.dto;

import java.util.List;

import lombok.Data;

@Data
public class ElementsDto {

	private Long id;
	private Long sectionId;
	private Long templateId;
	private String label;

	private String helpText;
	private String placeholder;
	private String type;
	private String subType;

	private Long sequence;
	private String createdBy;
	private String createdOn;
	private String modifiedBy;
	private String modifiedOn;

	private Boolean mandatoryFlag = false;
	private Boolean enableCommentsFlag = false;
	private Boolean enableFileUploadFlag = false;
	private Boolean optionFromMasterFlag = false;
	private Boolean optionFromMasterName = false;
	private Boolean familyResponseFlag = false;

	private Boolean spouseResponseRequiredFlag = false;
	private Boolean motherResponseRequiredFlag = false;
	private Boolean fatherResponseRequiredFlag = false;
	private Boolean motherinlawResponseRequiredFlag = false;
	private Boolean fatherinlawResponseRequiredFlag = false;
	private Boolean child1ResponseRequiredFlag = false;
	private Boolean child2ResponseRequiredFlag = false;

	private Boolean spouseResponseFlag = false;
	private Boolean motherResponseFlag = false;
	private Boolean fatherResponseFlag = false;
	private Boolean motherinlawResponseFlag = false;
	private Boolean fatherinlawResponseFlag = false;
	private Boolean child1ResponseFlag = false;
	private Boolean child2ResponseFlag = false;

	private List<ElementOptionsDto> values;
	private AssessmentElementResponseDto response;
	private String categoryName;
	private String questionWeightage;

	private List<ElementMasterValueDto> masterSetValues;

	private RespondentDetailsDTO respondentDetailsDTO;

	public ElementsDto() {
		//
	}
}
