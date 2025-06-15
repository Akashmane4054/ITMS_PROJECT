package com.ehr.assessment.business.dto;

import lombok.Data;

@Data
public class ElementOptionsDto {

	private Long id;

	private Long elementId;

	private String value;

	private String label;

	private String elementReasoningText;
	private Boolean allowOthersFlag;

	private Boolean selected = false;

	private String optionWeightage;

	private Integer sequence;

}
