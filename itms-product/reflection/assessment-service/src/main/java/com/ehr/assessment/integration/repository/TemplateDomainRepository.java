package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.TemplateDomainMapping;

@Repository
public interface TemplateDomainRepository extends JpaRepository<TemplateDomainMapping, Long> {

	@Modifying
	@Query("delete from TemplateDomainMapping su where su.templateId=?1")
	void deleteByTemplateId(Long templateId);

	@Modifying
	@Query("delete from TemplateDomainMapping su where su.templateId=?1 and domainId=?2")
	void deleteByTemplateIdAndDomainId(Long templateId, Long domainId);

	@Query("select su.domainId from TemplateDomainMapping su where su.templateId=?1")
	List<Long> findByTemplateId(Long templateId);
	
	List<TemplateDomainMapping> findByTemplateIdAndActive(Long templateId,Boolean active);

	@Query("select su.templateId from TemplateDomainMapping su where su.domainId=?1")
	List<Long> findByDomainId(Long domainId);

	TemplateDomainMapping findByTemplateIdAndDomainId(Long templateId, Long domainId);

}
