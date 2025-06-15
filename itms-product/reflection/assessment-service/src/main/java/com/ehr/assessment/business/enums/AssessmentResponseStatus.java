package com.ehr.assessment.business.enums;

public enum AssessmentResponseStatus {

	COMPLETED(1, "COMPLETED"),
	INPROGRESS(2, "INPROGRESS"),
	PENDING(3, "PENDING");

	private Integer id;
	private String name;

	private AssessmentResponseStatus(Integer id, String name) {
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
