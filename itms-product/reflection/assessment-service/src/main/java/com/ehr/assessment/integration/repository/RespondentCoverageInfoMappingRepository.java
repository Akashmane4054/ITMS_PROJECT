package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.business.enums.PolicyType;
import com.ehr.assessment.integration.domain.RespondentCoverageInfoMapping;

import jakarta.transaction.Transactional;

@Repository
public interface RespondentCoverageInfoMappingRepository extends JpaRepository<RespondentCoverageInfoMapping, Long> {

	List<RespondentCoverageInfoMapping> findByUserIdAndPolicyType(Long userId, PolicyType policyType);

    @Transactional
    @Modifying
    @Query("DELETE FROM RespondentCoverageInfoMapping r WHERE r.userId = :userId AND r.policyType = :policyType")
    void deleteByUserIdAndPolicyType(@Param("userId") Long userId, @Param("policyType") PolicyType policyType);

}
