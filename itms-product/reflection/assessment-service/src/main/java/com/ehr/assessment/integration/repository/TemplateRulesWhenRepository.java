package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.TemplateRulesWhen;


@Repository
public interface TemplateRulesWhenRepository extends JpaRepository<TemplateRulesWhen, Long> {
	
	@Query("select srw from TemplateRulesWhen srw where srw.templateRuleId=?1 order by srw.id asc")
	public List<TemplateRulesWhen> findByTemplateRuleId(Long templateRuleId);
	
	@Query("select srw from TemplateRulesWhen srw where srw.sectionId=?1 order by srw.id asc")
	public List<TemplateRulesWhen> findBySectionIdAndTemplateId(Long sectionId);
	
	public List<TemplateRulesWhen> findBySectionIdAndActionOn(Long sectionId,Long actionOn);
	
	public List<TemplateRulesWhen> findByTemplateId(Long templateId);
	
	TemplateRulesWhen findByIdAndActive(Long id,Boolean active);
	
	public List<TemplateRulesWhen> findByTemplateIdAndTemplateRuleId(Long templateId,Long templateRuleId);

}
