package com.ehr.assessment.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAndAnswersDTOForAISuggestion {

	private String question;

	private String answers;
}
