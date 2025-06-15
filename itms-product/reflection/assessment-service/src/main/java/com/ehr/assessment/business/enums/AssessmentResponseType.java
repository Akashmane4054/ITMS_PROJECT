package com.ehr.assessment.business.enums;

public enum AssessmentResponseType {
	SINGLE_CHOICE(1, "SINGLE_CHOICE"), // (Single answer)
	MULTIPLE_CHOICE(2, "MULTIPLE_CHOICE"), // (Multiple answers)
	DOCUMENT_UPLOAD(3, "DOCUMENT_UPLOAD"); // Upload a document as an answer

	private Integer id;
	private String name;

	private AssessmentResponseType(Integer id, String name) {
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
