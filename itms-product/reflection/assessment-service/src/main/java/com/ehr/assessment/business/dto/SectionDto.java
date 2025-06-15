package com.ehr.assessment.business.dto;

import java.util.List;

import org.springframework.beans.BeanUtils;

import lombok.Data;

@Data
public class SectionDto {

	private Long id;

	private String name;

	private String description;

	private Long templateId;

	private Long sequence;
	
	private Integer totalQuestionInOneSection;	
	private Integer responseCountInOneSection;

	private String createdBy;

	private String createdOn;

	private String modifiedBy;

	private String modifiedOn;
	
	private List<ElementsDto> sectionElements;
	
	private String sectionStatus;

	
	public SectionDto() {
	}

	public SectionDto(SectionDto section) {
		BeanUtils.copyProperties(section, this);
	}

}
