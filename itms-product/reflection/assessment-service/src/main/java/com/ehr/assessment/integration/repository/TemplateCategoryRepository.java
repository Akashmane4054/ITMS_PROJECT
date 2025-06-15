package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.TemplateCategoryMapping;

@Repository
public interface TemplateCategoryRepository extends JpaRepository<TemplateCategoryMapping, Long> {

	@Modifying
	@Query("delete from TemplateCategoryMapping su where su.templateId=?1")
	void deleteByTemplateId(Long templateId);

	@Modifying
	@Query("delete from TemplateCategoryMapping su where su.templateId=?1 and categoryId=?2")
	void deleteByTemplateIdAndCategoryId(Long templateId, Long categoryId);

	@Query("select su.categoryId from TemplateCategoryMapping su where su.templateId=?1")
	List<Long> findByTemplateId(Long templateId);

	@Query("select su.templateId from TemplateCategoryMapping su where su.categoryId=?1")
	List<Long> findByCategoryId(Long categoryId);

	TemplateCategoryMapping findByTemplateIdAndCategoryId(Long templateId, Long categoryId);
	
	List<TemplateCategoryMapping> findByTemplateIdAndActive(Long templateId,Boolean active);

}
