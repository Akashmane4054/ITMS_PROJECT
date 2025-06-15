package com.ehr.assessment.business.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireDTOForAISuggestion {

	private String suggestionRequired;

	private List<QuestionAndAnswersDTOForAISuggestion> questionAndAnswers;
}
