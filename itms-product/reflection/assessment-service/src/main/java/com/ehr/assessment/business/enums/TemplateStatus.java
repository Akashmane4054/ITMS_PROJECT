package com.ehr.assessment.business.enums;

public enum TemplateStatus {

	DRAFT(1, "DRAFT"), PUBLISHED(2, "PUBLISHED");

	private Integer id;
	private String name;

	private TemplateStatus(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
