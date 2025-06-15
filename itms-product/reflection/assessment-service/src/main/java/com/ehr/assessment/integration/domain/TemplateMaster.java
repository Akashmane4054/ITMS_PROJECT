package com.ehr.assessment.integration.domain;

import java.util.Date;

import com.ehr.assessment.business.enums.TemplateStatus;
import com.ehr.assessment.business.enums.TemplateType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = { @Index(name = "template_master_index_1", columnList = "status"),
		@Index(name = "template_master_index_2", columnList = "active") })
public class TemplateMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Column(length = 1000)
	private String description;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private TemplateType templateType;

	private Integer versionNumber;

	@NotNull
	@Enumerated(EnumType.STRING)
	private TemplateStatus status;

	private Long companyId;

	private Boolean selfReview;

	private Long reviewer;

	private Date reviewedOn;

	private Long createdBy;

	private Date createdOn;

	private Long modifiedBy;

	private Date modifiedOn;

	private Boolean active = true;

}
