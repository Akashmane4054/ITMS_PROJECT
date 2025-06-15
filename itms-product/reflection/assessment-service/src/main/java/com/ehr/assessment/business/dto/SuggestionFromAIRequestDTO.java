package com.ehr.assessment.business.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionFromAIRequestDTO {

	private RespondentUserProfileDetailsDTOForAISuggestion profileDetails;

	private List<Map<String, QuestionnaireDTOForAISuggestion>> questionnaire;

	private String overallSuggestionExpectedOn;

	private String explanation;

	private String responseStructure;
}
