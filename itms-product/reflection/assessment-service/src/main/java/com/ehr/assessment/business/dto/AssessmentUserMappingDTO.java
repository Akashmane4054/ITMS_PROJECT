package com.ehr.assessment.business.dto;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssessmentUserMappingDTO {

	private Long id;
	private Long userId;
	private Long assessmentId;
	private Integer assessmentResponseStatus;
	private Boolean emailReminder;
	private Date submittedOn;
	private String name;
	private Date lastReminderSent;

	private Long companyId;

	public String getLastReminderSent() {
		return formatDate(lastReminderSent);
	}

	private String formatDate(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
		return dateFormat.format(date);
	}

}
