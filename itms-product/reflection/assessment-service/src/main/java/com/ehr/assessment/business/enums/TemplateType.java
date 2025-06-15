package com.ehr.assessment.business.enums;

public enum TemplateType {

	FREE(1, "Free"), PREMIUM(2, "Premium");

	private Integer id;
	private String name;

	private TemplateType(Integer id, String name) {
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
