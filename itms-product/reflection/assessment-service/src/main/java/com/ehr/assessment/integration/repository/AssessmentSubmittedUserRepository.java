package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.AssessmentSubmittedResponseUser;

@Repository
public interface AssessmentSubmittedUserRepository extends JpaRepository<AssessmentSubmittedResponseUser, Long> {

	@Modifying
	@Query("delete from AssessmentSubmittedResponseUser su where su.assessmentId=?1")
	void deleteByAssessmentId(Long assessmentId);

	@Query("select su.userId from AssessmentSubmittedResponseUser su where su.assessmentId=?1")
	List<Long> findByAssessmentId(Long assessmentId);

	AssessmentSubmittedResponseUser findByAssessmentIdAndUserId(Long assessmentId, Long userId);

	@Query("select su.assessmentId from AssessmentSubmittedResponseUser su where su.userId=?1")
	List<Long> findByUserId(Long userId);

}
