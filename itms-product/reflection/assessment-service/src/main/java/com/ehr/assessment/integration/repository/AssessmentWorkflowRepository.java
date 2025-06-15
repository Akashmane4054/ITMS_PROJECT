package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.AssessmentWorkFlow;

@Repository
public interface AssessmentWorkflowRepository extends JpaRepository<AssessmentWorkFlow, Long>{

	@Query("select r from AssessmentWorkFlow r where r.status=?1 and r.assessmentId=?2")
	AssessmentWorkFlow findByAssessmentAndStatus(Integer status, Long assessmentId);

	@Query("select r from AssessmentWorkFlow r where r.assessmentId=?1 ")
	List<AssessmentWorkFlow> findByAssessmentId(Long assessmentId);

	void deleteByAssessmentIdAndStatus(Long assessmentId, Integer status);
}
