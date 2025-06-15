package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.business.enums.TemplateType;
import com.ehr.assessment.integration.domain.TemplateMaster;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateMaster, Long> {

	public TemplateMaster findByIdAndActive(Long id, Boolean active);
	
	public TemplateMaster findByNameAndCompanyIdAndActive(String name,Long companyId,Boolean active);
	
	public TemplateMaster findByIdAndCompanyIdAndActive(Long templateId,Long companyId, Boolean active);

	public TemplateMaster findByIdAndCompanyId(Long templateId,Long companyId);

	@Query("select max(id) FROM TemplateMaster")
	public Long getMaxId();

	@Query("SELECT s.id FROM TemplateMaster s WHERE s.id = :templateId")
	public Long findTemplateMasterIdByTemplateMasterId(@Param("templateId") Long templateId);

	public List<TemplateMaster> findByActive(@Param("active") boolean active);

	@Query("SELECT s.name FROM TemplateMaster s WHERE s.id = :templateId")
	public String findNameByTemplateMasterId(@Param("templateId") Long templateId);

	@Query("SELECT s.id FROM TemplateMaster s WHERE s.id IN :templateIds AND s.active = true AND s.createdBy = :userId ")
	List<Long> findActiveTemplateMastersByUser(@Param("templateIds") List<Long> templateIds,
			@Param("userId") Long userId);

	@Query("SELECT t FROM TemplateMaster t WHERE t.companyId = :companyId")
	List<TemplateMaster> findByCompanyId(@Param("companyId") Long companyId);

	List<TemplateMaster> findByActiveAndTemplateType(Boolean status, TemplateType templateType);

	@Query("SELECT t FROM TemplateMaster t WHERE t.id IN :ids")
	List<TemplateMaster> findByIds(@Param("ids") List<Long> ids);

	public TemplateMaster findByNameAndActive(String name, Boolean active);

}
