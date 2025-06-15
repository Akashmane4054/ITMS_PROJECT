package com.ehr.companymanagement.business.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ColumnDetailsDTO implements Serializable {
	private String dataIndex;
	private String width;
	private String title;
	private String rowKey;

	private Long sequenceColumn;

}
