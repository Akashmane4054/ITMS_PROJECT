package com.ehr.companymanagement.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.companymanagement.integration.domain.CompanyDetails;

@Repository
public interface CompanyDetailsRepository extends JpaRepository<CompanyDetails, Long> {

    CompanyDetails findByIdAndActive(Long id, Boolean active);
    
    CompanyDetails findByCompanyNameAndActive(String companyName, Boolean active);
    
    @Query("SELECT cd FROM CompanyDetails cd WHERE cd.companyName = :companyName AND cd.id = :id AND cd.active = true")
    CompanyDetails findByCompanyNameAndIdAndActive(@Param("companyName") String companyName, @Param("id") Long id);


    @Query("select c.adminId from CompanyDetails c where c.active=true")
    List<Long> findAllAdminId();

    @Query("select c.id from CompanyDetails c where c.subId=?1")
    List<Long> findBySubId(Long subId);

    @Query("select c.companyName from CompanyDetails c where c.id in (?1)")
    List<String> findCompanyNamesByCompanyIds(List<Long> companyIds);

    @Query("select c.addressId from CompanyDetails c where c.id=?1")
    Long findAddressIdByCompanyId(Long companyId);

    @Query("select c.adminId from CompanyDetails c where c.active=false")
    List<Long> findAllAdminIdInToInActiveCompany();

    boolean existsByIdAndActive(Long id, Boolean active);
    
    @Query("select at from CompanyDetails at where at.id=?1 and at.active=true")
   	CompanyDetails findByIdAndActive(Long companyId);
    
    @Query("select at from CompanyDetails at where at.subId=?1 and at.active=true")
   	List<CompanyDetails> findBySubIdAndActive(Long subId);
    
}
