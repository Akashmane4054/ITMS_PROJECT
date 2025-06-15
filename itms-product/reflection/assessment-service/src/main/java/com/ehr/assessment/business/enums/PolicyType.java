package com.ehr.assessment.business.enums;

public enum PolicyType {

	CORPORATE(1, "CORPORATE"), 
	PERSONAL(2, "PERSONAL");

	private Integer id;
	private String name;
	
	private PolicyType(Integer id, String name) {
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
