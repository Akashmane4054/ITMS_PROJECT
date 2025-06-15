package com.ehr.assessment.integration.repository;

import com.ehr.assessment.integration.domain.SectionWiseRiskMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionWiseRiskMatrixRepository extends JpaRepository<SectionWiseRiskMatrix, Long> {
	
    SectionWiseRiskMatrix findBysectionIdAndAssessmentIdAndRiskScore(Long sectionId, Long assessmentId, Long riskScore);
    
}
