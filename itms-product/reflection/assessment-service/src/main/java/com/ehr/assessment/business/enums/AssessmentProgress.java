package com.ehr.assessment.business.enums;

public enum AssessmentProgress {
	
	BASIC_INFO(1, "BASIC_INFO"), 
	TEMPLATE(2, "TEMPLATE"),
	QUESTIONAIRE(3, "QUESTIONAIRE"), 
	RESPONDENTS(4, "RESPONDENTS"), 
	SCHEDULE(5, "SCHEDULE");

	private Integer id;
	private String name;
	
	
	private AssessmentProgress(Integer id, String name) {
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
