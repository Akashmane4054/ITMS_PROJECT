/**
 *
 */
package com.ehr.companymanagement.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.companymanagement.integration.domain.SubscriptionPlans;

/**
 * @author Maroof
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlans, Long> {

    SubscriptionPlans findBySubscriptionPlanIdAndCompanyId(Long subscriptionPlanId, Long companyId);

    SubscriptionPlans findBySubscriptionPlanIdAndActive(Long subscriptionPlanId, Boolean active);
    
    SubscriptionPlans findBySubscriptionPlanId(Long subscriptionPlanId);

    @Query("select sp from SubscriptionPlans sp where sp.planName=?1 and sp.companyId=?2")
    SubscriptionPlans findDuplicateNameAdd(String planName, Long companyId);

    @Query("select sp from SubscriptionPlans sp where sp.planName=?1 and sp.companyId=?2 and sp.subscriptionPlanId!=?3")
    SubscriptionPlans findDuplicateNameEdit(String planName, Long companyId, Long subscriptionPlanId);

    @Query("select sp from SubscriptionPlans sp where sp.planName LIKE %:planName%")
    List<SubscriptionPlans> findByPlanName(@Param("planName") String planName);

    @Query("select sp.subscriptionPlanId from SubscriptionPlans sp where sp.active=true")
    List<Long> findAllSubscriptionPlansIds();

    @Query("select sp from SubscriptionPlans sp where sp.planName=?1")
    SubscriptionPlans findOneByPlanName(String planName);
    
    @Query("select sp.planName from SubscriptionPlans sp where sp.active=true and sp.subscriptionPlanId = ?1")
    String findPlanNameByIdAndIsTrue(Long id);
    

}
