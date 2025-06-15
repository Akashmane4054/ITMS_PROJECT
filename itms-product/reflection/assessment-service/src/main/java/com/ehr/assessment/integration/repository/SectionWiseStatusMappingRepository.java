package com.ehr.assessment.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.SectionWiseStatusMapping;

import feign.Param;

@Repository
public interface SectionWiseStatusMappingRepository extends JpaRepository<SectionWiseStatusMapping, Long> {

	SectionWiseStatusMapping findBysectionIdAndAssessmentIdAndUserId(Long sectionId, Long assessmentId, Long userId);

	@Query("SELECT COALESCE(s.responseCountInOneSection, 0) FROM SectionWiseStatusMapping s WHERE s.sectionId = :sectionId AND s.assessmentId = :assessmentId AND s.userId = :userId")
	Integer findResponseCountInOneSection(@Param("sectionId") Long sectionId, @Param("assessmentId") Long assessmentId,
			@Param("userId") Long userId);

	@Modifying
	@Query("UPDATE SectionWiseStatusMapping s SET s.sectionResponseStatus = 'PENDING' WHERE s.sequence > :sequence AND s.assessmentId = :assessmentId AND s.userId = :userId")
	void updateStatusBySequenceGreaterThanAndAssessmentIdAndUserId(@Param("sequence") Long sequence,
			@Param("assessmentId") Long assessmentId, @Param("userId") Long userId);

}
