package com.ehr.assessment.integration.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Every Element is the question inside Section ( SurveyElement)
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = { @Index(name = "element_index_1", columnList = "sectionId,active") })
public class Elements {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long sectionId;

	private Long templateId;

	private String type;
	private String subType;

	@Column(length = 10000)
	private String label;

	private String helpText;

	private String placeholder;

	private Long sequence;

	private Boolean mandatoryFlag;

	private Boolean enableCommentsFlag;

	private Boolean commentsMandatoryFlag;

	private Boolean enableFileUploadFlag;

	private Boolean fileUploadMandatoryFlag;

	private Boolean optionFromMasterFlag;

	private Boolean optionFromMasterName;

	private Boolean familyResponseFlag;

	private Boolean spouseResponseRequiredFlag;

	private Boolean motherResponseRequiredFlag;

	private Boolean fatherResponseRequiredFlag;

	private Boolean motherinlawResponseRequiredFlag;

	private Boolean fatherinlawResponseRequiredFlag;

	private Boolean child1ResponseRequiredFlag;

	private Boolean child2ResponseRequiredFlag;

	private Boolean spouseResponseFlag;
	private Boolean motherResponseFlag;
	private Boolean fatherResponseFlag;
	private Boolean motherinlawResponseFlag;
	private Boolean fatherinlawResponseFlag;
	private Boolean child1ResponseFlag;
	private Boolean child2ResponseFlag;

	private String categoryName;

	private String questionWeightage;

	private Boolean active = true;

	private Long createdBy;

	private Date createdOn;

	private Long modifiedBy;

	private Date modifiedOn;

}
