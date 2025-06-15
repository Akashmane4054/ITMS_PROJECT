package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.Section;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

	public Section findByIdAndTemplateIdAndActive(Long id, Long templateId, Boolean active);

	public Section findByIdAndActive(Long id, Boolean active);

	public List<Section> findByTemplateIdAndActive(Long templateId, Boolean active);

	@Modifying
	@Query("delete from Section sp where sp.templateId=?1")
	public void deleteSectionsByTemplateId(Long templateId);

	public List<Section> findByTemplateId(Long templateId);

	public Section findByNameAndTemplateIdAndActive(String name, Long templateId, Boolean active);

	@Query("SELECT s.id FROM Section s WHERE s.templateId = :templateId AND s.active = true")
	List<Long> findIdsByTemplateId(@Param("templateId") Long templateId);

	@Query("SELECT s.id FROM Section s WHERE s.active = true AND s.templateId = :templateId ORDER BY s.sequence DESC LIMIT 1")
	Long findIdByTemplateIdAndActiveTrueOrderBySequenceDescLimit1(@Param("templateId") Long templateId);

	@Query("SELECT s.templateId FROM Section s WHERE s.id = :id AND s.active = true")
	Long findTemplateIdBySectionIdAndActiveTrue(@Param("id") Long id);
}
