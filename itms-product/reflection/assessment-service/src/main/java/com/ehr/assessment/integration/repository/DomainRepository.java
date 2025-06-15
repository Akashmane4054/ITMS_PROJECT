package com.ehr.assessment.integration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.DomainMaster;

@Repository
public interface DomainRepository extends JpaRepository<DomainMaster, Long> {

	DomainMaster findByDomainIdAndActive(Long domainId, Boolean active);

	DomainMaster findByDomainName(String domainName);

	Optional<DomainMaster> findByDomainId(Long id);

	List<DomainMaster> findByActive(boolean active);

	@Query("SELECT c.domainName FROM DomainMaster c WHERE c.domainId IN :domainIds")
	List<String> findNamesByIds(@Param("domainIds") List<Long> domainIds);
}
