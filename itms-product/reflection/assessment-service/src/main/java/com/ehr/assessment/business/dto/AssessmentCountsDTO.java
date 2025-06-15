package com.ehr.assessment.business.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Data;

@Data
public class AssessmentCountsDTO {

	private Long assessmentSentTo;
	private Long assessmentResponded;
	private Long assessmentInProgress;
	private Long assessmentNotStarted;
	private Double completionPercentage;

	public void calculateCompletionPercentage() {
		if (assessmentSentTo != null && assessmentSentTo > 0 && assessmentResponded != null) {
			BigDecimal percentage = BigDecimal
					.valueOf((assessmentResponded.doubleValue() / assessmentSentTo.doubleValue()) * 100)
					.setScale(2, RoundingMode.HALF_UP);
			this.completionPercentage = percentage.doubleValue();
		} else {
			this.completionPercentage = 0.0;
		}
	}

}