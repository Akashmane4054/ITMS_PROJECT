package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.AssessmentElementResponse;
import com.ehr.core.dto.RawTypeFieldDTO;

@Repository
public interface AssessmentElementResponseRepository extends JpaRepository<AssessmentElementResponse, Long> {

	public AssessmentElementResponse findByIdAndElementId(Long id, Long elementId);

	public AssessmentElementResponse findByCreatedByAndElementId(Long createdBy, Long elementId);

	public AssessmentElementResponse findByElementId(Long elementId);

	@Modifying
	@Query("delete from AssessmentElementResponse sea where sea.createdBy=?1 and sea.sectionId=?2 and sea.assessmentId=?3")
	public void deleteAssessment(Long createdBy, Long sectionId, Long assessmentId);

	@Query("select max(id) FROM AssessmentElementResponse")
	public Long getMaxId();

	public List<AssessmentElementResponse> findByCreatedByAndAssessmentId(Long createdBy, Long assessmentId);

	@Query("SELECT sa.assessmentWeightage FROM AssessmentElementResponse sa WHERE sa.elementId = ?1 and sa.createdBy=?2 and sa.assessmentId=?3")
	public String findAssessmentWeightageByElementIdAndCreatedByAndAssessmentId(Long elementId, Long createdBy,
			Long assessmentId);

	public AssessmentElementResponse findByCreatedByAndElementIdAndAssessmentId(Long createdBy, Long elementId,
			Long assessmentId);

	@Query("SELECT sea FROM AssessmentElementResponse sea "
			+ "WHERE sea.assessmentId = :assessmentId AND sea.createdBy = :createdBy " + "ORDER BY sea.createdOn DESC")
	List<AssessmentElementResponse> findLatestByAssessmentIdAndCreatedBy(@Param("assessmentId") Long assessmentId,
			@Param("createdBy") Long createdBy);

	@Query("SELECT sea FROM AssessmentElementResponse sea " + "WHERE sea.elementId = :elementId "
			+ "AND sea.assessmentId = :assessmentId " + "AND sea.createdBy = :createdBy "
			+ "ORDER BY sea.createdOn DESC")
	AssessmentElementResponse findLatestByElementIdAndAssessmentIdAndCreatedBy(@Param("elementId") Long elementId,
			@Param("assessmentId") Long assessmentId, @Param("createdBy") Long createdBy);

	@Query("SELECT sa.assessmentWeightage FROM AssessmentElementResponse sa WHERE sa.elementId = ?1")
	public String findAssessmentWeightageByElementId(Long elementId);

	public List<AssessmentElementResponse> findByAssessmentId(Long assessmentId);

	public List<AssessmentElementResponse> findByAssessmentIdAndElementId(Long assessmentId, Long elementId);

	@Query("SELECT new com.ehr.core.dto.RawTypeFieldDTO(a.elementId, a.response) "
			+ "FROM AssessmentElementResponse a WHERE a.createdBy = ?1 AND a.assessmentId = ?2")
	List<RawTypeFieldDTO<Long, String, ?>> findElementIdAndResponseByCreatedByAndAssessmentId(Long createdBy,
			Long assessmentId);
}
