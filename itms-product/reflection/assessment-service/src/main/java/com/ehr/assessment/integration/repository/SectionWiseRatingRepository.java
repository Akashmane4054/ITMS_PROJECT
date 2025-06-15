package com.ehr.assessment.integration.repository;

import com.ehr.assessment.integration.domain.SectionWiseRating;

import feign.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionWiseRatingRepository extends JpaRepository<SectionWiseRating, Long> {

	SectionWiseRating findBySectionIdAndAssessmentIdAndUserId(Long sectionId, Long assessmentId, Long userId);

	@Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SectionWiseRating s WHERE s.assessmentId = :assessmentId AND s.userId = :userId")
	boolean existsByAssessmentIdAndUserId(@Param("assessmentId") Long assessmentId, @Param("userId") Long userId);
}
