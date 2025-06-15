package com.ehr.companymanagement.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ehr.companymanagement.integration.domain.CompanyPaymentDetails;

@Repository
public interface CompanyPaymentDetailsRepository extends JpaRepository<CompanyPaymentDetails, Long> {

    List<CompanyPaymentDetails> findByCompanyIdAndActive(Long companyId, Boolean active);

    CompanyPaymentDetails findByCompanyIdAndIdAndActive(Long companyId, Long id, Boolean active);
    
    CompanyPaymentDetails findByIdAndActive(Long id, Boolean active);
    
    @Query("select p from CompanyPaymentDetails p where p.active = true and p.companyId=?1")
    CompanyPaymentDetails findByCompanyIdAndActivetrue(Long companyId);

}
