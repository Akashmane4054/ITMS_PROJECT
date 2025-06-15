package com.ehr.assessment.integration.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Here we store the options possible for Elements (SurveyElementMaster) 
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(indexes = {@Index(name = "element_options_index_1",  columnList="elementId")})
public class ElementOptions {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long elementId;
	
	private String value;

	private String label;
	
	private String elementReasoningText;
	private Boolean allowOthersFlag;

	private Boolean selected;

	private String optionWeightage;

	private Integer sequence;
}
