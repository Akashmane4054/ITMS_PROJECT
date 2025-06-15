package com.ehr.companymanagement.integration.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.companymanagement.integration.domain.CompanySubscriptionMapping;

@Repository
public interface CompanySubscriptionMappingRepository extends JpaRepository<CompanySubscriptionMapping, Long> {

    @Query("select c from CompanySubscriptionMapping c where c.active=true and c.companyId=?1")
    CompanySubscriptionMapping findByCurrentActiveSubscriptionCompanyId(Long companyId);

    CompanySubscriptionMapping findByCompanyIdAndSubscriptionPlanIdAndActive(Long companyId, Long subscriptionPlanId,
            Boolean active);

	CompanySubscriptionMapping findByCompanyIdAndActive(Long companyId, Boolean active);
	
	@Query("select cs.companyId from CompanySubscriptionMapping cs where cs.subscriptionPlanId in (?1) and cs.active=true")
	List<Long> findbyListOfSubscriptionPlanId(List<Long> collect);
	
	@Query("select a.companyId from CompanySubscriptionMapping a where a.active=true and a.validFrom >= :validFrom")
	List<Long> findByValidFromdate(@Param("validFrom") Date validFrom);
	
	@Query("select a.companyId from CompanySubscriptionMapping a where a.active=true and a.validTo <= :validTo")
	List<Long> findByValidTodate(@Param("validTo") Date validTo);
	
	@Query("select cs.companyId from CompanySubscriptionMapping cs where cs.subscriptionPlanId in (?1)")
	List<Long> findCompanyIdsBySubscriptions(List<Long> subscriptions);
	
	@Query("select cs from CompanySubscriptionMapping cs where cs.subscriptionPlanId in (?1) and cs.active=true")
	List<CompanySubscriptionMapping> findCompanysBySubscriptions(List<Long> subscriptions);
	
	@Query("select cs.companyId from CompanySubscriptionMapping cs where cs.subscriptionPlanId=?1 and cs.active=?2")
	List<Long> findCompanyIdsBySubscriptionPlanId(Long subscriptionPlanId, Boolean active);
	
	@Query("select cs.companyId from CompanySubscriptionMapping cs where cs.subscriptionPlanId=?1")
	List<Long> findCompanyIdsBySubscriptionPlanId(Long subscriptionPlanId);
	
	@Query("SELECT csm.companyId FROM CompanySubscriptionMapping csm WHERE csm.validFrom BETWEEN :startDate AND :endDate")
	List<Long> findCompanyIdsByValidFromBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	@Query("SELECT csm.companyId FROM CompanySubscriptionMapping csm WHERE csm.validTo BETWEEN :startDate AND :endDate")
	List<Long> findCompanyIdsByValidToBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);


}
