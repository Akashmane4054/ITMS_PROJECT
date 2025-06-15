package com.ehr.assessment.business.enums;

public enum AssessmentStatus {
	DRAFT(1, "DRAFT"), 
	FOR_REVIEW(2, "FOR_REVIEW"),
	SCHEDULED(3, "SCHEDULED"), 
	ACTIVE(4, "ACTIVE"), 
	CLOSED(5, "CLOSED");


	private Integer statusCode;
	private String status;

	AssessmentStatus(Integer statusCode, String status) {
		this.statusCode = statusCode;
		this.status = status;
	}

	public static AssessmentStatus getByStatusCode(Integer statusCode) {
		for (AssessmentStatus e : values()) {
			if (e.statusCode.equals(statusCode))
				return e;
		}
		return DRAFT;
	}

	public Integer getStatusCode() {
		return statusCode;
	}
	
	public String getStatus() {
		return status;
	}

}
