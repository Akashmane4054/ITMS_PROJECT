package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.TemplateRules;

@Repository
public interface TemplateRulesRepository extends JpaRepository<TemplateRules, Long> {

	List<TemplateRules> findByTemplateIdAndActive(Long templateId, Boolean active);

	TemplateRules findByIdAndActive(Long templateId, Boolean active);

	TemplateRules findByIdAndTemplateId(Long id, Long templateId);

}
