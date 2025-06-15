package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.business.enums.AssessmentStatus;
import com.ehr.assessment.integration.domain.Assessment;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

	public Assessment findByIdAndCompanyIdAndActive(Long id, Long companyId, Boolean active);

	public Assessment findByIdAndActive(Long id, Boolean active);

	public Assessment findByIdAndCompanyId(Long assessmentId, Long companyId);

	@Query("select max(id) FROM Assessment")
	public Long getMaxId();

	public List<Assessment> findByOwner(Long owner);

	public Assessment findByNameAndCompanyId(String name, Long companyId);

	@Query("SELECT COUNT(u) FROM Assessment u WHERE u.companyId = ?1 ")
	public Long companyAssessmentCount(Long companyId);

	public Assessment findByNameAndCompanyIdAndActive(String name, Long companyId, Boolean active);

	@Query("SELECT s.templateId FROM Assessment s WHERE s.id = :assessmentId")
	public Long findTemplateIdByAssessmentId(@Param("assessmentId") Long assessmentId);

	@Query("SELECT s.name FROM Assessment s WHERE s.id = :assessmentId")
	public String findNameByAssessmentId(@Param("assessmentId") Long assessmentId);

	@Query("select max(a.assessmentDisplayIdSequence) from Assessment a WHERE a.companyId = :companyId AND a.financialYear = :financialYear")
	Long findMaxAssessmentDisplayIdSequence(@Param("companyId") Long companyId,
			@Param("financialYear") String financialYear);

	@Query("select a.assessmentDisplayId from Assessment a where a.assessmentDisplayIdSequence= :assessmentDisplayIdSequence AND a.companyId = :companyId")
	String findAssessmentDisplayIdByAssessmentDisplayIdSequence(
			@Param("assessmentDisplayIdSequence") Long assessmentDisplayIdSequence, @Param("companyId") Long companyId);

	@Query("SELECT s.id FROM Assessment s WHERE s.id IN :assessmentIds AND s.active = true AND (s.reviewer = :userId OR s.owner = :userId)")
	List<Long> findActiveAssessmentsByUser(@Param("assessmentIds") List<Long> assessmentIds,
			@Param("userId") Long userId);

	@Query("SELECT su FROM Assessment su WHERE su.name LIKE %:name% AND su.companyId = :companyId")
	public List<Assessment> findByNameAndCompanyIds(@Param("name") String name, @Param("companyId") Long companyId);

	@Query("SELECT v.id, v.expiredOn FROM Assessment v WHERE v.id IN :id ")
	List<Object[]> findIdsAndExpiredOnByAssessmentIds(@Param("id") List<Long> id);

	@Query("SELECT a FROM Assessment a WHERE a.status = :status AND a.active = true")
	public List<Assessment> findByStatusCodeAndActive(@Param("status") AssessmentStatus status);

	@Query("SELECT a FROM Assessment a WHERE a.status = :status AND a.scheduledOn <= :currentTime")
	List<Assessment> findScheduledAssessmentsToActivate(@Param("status") Integer status,
			@Param("currentTime") long currentTime);
	
}
