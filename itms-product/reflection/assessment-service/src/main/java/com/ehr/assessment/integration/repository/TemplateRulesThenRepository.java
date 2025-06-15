package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.TemplateRulesThen;


@Repository
public interface TemplateRulesThenRepository extends JpaRepository<TemplateRulesThen, Long> {
	public List<TemplateRulesThen> findByTemplateRuleId(Long templateRuleId);
	
	public List<TemplateRulesThen> findBySectionIdAndTemplateId(Long sectionId,Long templateId);
	
	public List<TemplateRulesThen> findByTemplateId(Long templateId);
	
	public TemplateRulesThen findByIdAndActive(Long id,Boolean active);
	
	public List<TemplateRulesThen> findByTemplateIdAndTemplateRuleId(Long templateId,Long templateRuleId);

}
