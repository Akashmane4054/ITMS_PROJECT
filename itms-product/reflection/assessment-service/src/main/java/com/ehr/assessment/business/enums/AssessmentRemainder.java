package com.ehr.assessment.business.enums;

public enum AssessmentRemainder {

	FIRST(1, "First Reminder"), SECOND(2, "Second Reminder"), FINAL(3, "Final Reminder");

	private Integer id;
	private String name;

	private AssessmentRemainder(Integer id, String name) {
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
