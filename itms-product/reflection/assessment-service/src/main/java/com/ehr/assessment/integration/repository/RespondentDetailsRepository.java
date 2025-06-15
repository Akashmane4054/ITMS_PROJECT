package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.business.dto.ExternalProjectionDTO;
import com.ehr.assessment.business.dto.RespondentProjectionDTO;
import com.ehr.assessment.integration.domain.RespondentDetails;

@Repository
public interface RespondentDetailsRepository extends JpaRepository<RespondentDetails, Long> {

	public RespondentDetails findByUserId(Long userId);

	@Query("SELECT new com.ehr.assessment.business.dto.ExternalProjectionDTO("
			+ "r.userId, CONCAT(r.firstName, ' ', r.lastName), r.emailAddress) "
			+ "FROM RespondentDetails r WHERE r.userId IN :userIds")
	List<ExternalProjectionDTO> findByUserIds(@Param("userIds") List<Long> userIds);

	@Query("SELECT new com.ehr.assessment.business.dto.RespondentProjectionDTO("
			+ "r.userId, r.emailAddress, r.dateOfBirth ,r.gender) "
			+ "FROM RespondentDetails r WHERE r.userId IN :userIds")
	List<RespondentProjectionDTO> findDetailsByUserIds(@Param("userIds") List<Long> userIds);

	@Query("SELECT new com.ehr.assessment.business.dto.ExternalProjectionDTO("
			+ "r.userId, CONCAT(r.firstName, ' ', r.lastName), r.emailAddress) "
			+ "FROM RespondentDetails r WHERE r.userId = :userId")
	ExternalProjectionDTO findDetailsByUserId(@Param("userId") Long userId);

}
