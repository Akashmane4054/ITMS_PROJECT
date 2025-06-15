package com.ehr.assessment.integration.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.business.enums.AssessmentResponseStatus;
import com.ehr.assessment.integration.domain.AssessmentUserMapping;

@Repository
public interface AssessmentUserRepository extends JpaRepository<AssessmentUserMapping, Long> {

	@Modifying
	@Query("delete from AssessmentUserMapping su where su.assessmentId=?1")
	void deleteByAssessmentId(Long assessmentId);

	@Modifying
	@Query("delete from AssessmentUserMapping su where su.assessmentId=?1 and userId=?2")
	void deleteByAssessmentIdAndUserId(Long assessmentId, Long userId);

	@Query("select su.userId from AssessmentUserMapping su where su.assessmentId=?1")
	List<Long> findByAssessmentId(Long assessmentId);

	@Query("select su.assessmentId from AssessmentUserMapping su where su.userId=?1")
	List<Long> findByUserId(Long userId);

	AssessmentUserMapping findByAssessmentIdAndUserId(Long assessmentId, Long userId);

	List<Long> findSubmittedUserIdsByAssessmentId(Long assessmentId);

	@Query("SELECT a FROM AssessmentUserMapping a WHERE a.assessmentResponseStatus <> :status AND a.lastReminderSent < :lastReminderSent")
	List<AssessmentUserMapping> findByAssessmentResponseStatusNotAndLastReminderSentBefore(
	        @Param("status") AssessmentResponseStatus status,
	        @Param("lastReminderSent") Date lastReminderSent);


	@Modifying
	@Query("DELETE FROM AssessmentUserMapping su WHERE su.assessmentId = :assessmentId AND su.userId IN :userIds")
	void deleteAllByAssessmentIdAndUserIdIn(Long assessmentId, Set<Long> userIds);

	@Query("select su from AssessmentUserMapping su where su.assessmentId=?1")
	List<AssessmentUserMapping> findByAssessmentIds(Long assessmentId);

	List<AssessmentUserMapping> findByUserIdAndActive(Long userId, Boolean active);

	List<AssessmentUserMapping> findByAssessmentIdAndCreatedBy(Long assessmentId, Long createdBy);

	@Query("SELECT a.userId FROM AssessmentUserMapping a WHERE a.assessmentId = :assessmentId AND a.createdBy = :createdBy")
	List<Long> findUserIdsByAssessmentIdAndCreatedBy(@Param("assessmentId") Long assessmentId,
			@Param("createdBy") Long createdBy);

	@Query("SELECT a.assessmentId FROM AssessmentUserMapping a WHERE a.userId = :userId")
	List<Long> findAssessmentIdsByUserId(Long userId);

	@Query("SELECT COUNT(a) FROM AssessmentUserMapping a WHERE a.assessmentId = :assessmentId")
	Long countByAssessmentId(@Param("assessmentId") Long assessmentId);

	@Query("SELECT COUNT(a) FROM AssessmentUserMapping a WHERE a.assessmentId = :assessmentId AND a.assessmentResponseStatus = :status")
	Long countByAssessmentIdAndAssessmentResponseStatus(@Param("assessmentId") Long assessmentId,
			@Param("status") AssessmentResponseStatus status);

	@Query("SELECT a.userId FROM AssessmentUserMapping a WHERE a.assessmentId = :assessmentId AND (a.assessmentResponseStatus IS NULL OR a.assessmentResponseStatus = :status)")
	List<Long> findUserIdsByAssessmentId(@Param("assessmentId") Long assessmentId,
			@Param("status") AssessmentResponseStatus status);

	@Query("SELECT a FROM AssessmentUserMapping a WHERE a.userId = :userId AND a.assessmentId = :assessmentId")
	AssessmentUserMapping findAssessmentUserMappingByUserIdAndAssessmentId(@Param("userId") Long userId,
			@Param("assessmentId") Long assessmentId);

}
