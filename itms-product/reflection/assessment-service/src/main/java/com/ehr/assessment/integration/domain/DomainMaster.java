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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = {@Index(name = "domain_index_1",  columnList="domainId,active")})
public class DomainMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long domainId;
	private String domainName;
	@Column(length = 1000)
	private String description;

	private Long createdBy;

	private Date createdOn;

	private Long modifiedBy;

	private Date modifiedOn;

	private Boolean active=true;

}
