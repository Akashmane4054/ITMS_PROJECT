package com.ehr.assessment.presentation.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropsValue {

    @Value("${aiSuggestion.assessment.explanation}")
    public String explanationToAiForSuggestionOnAssessment;
}
