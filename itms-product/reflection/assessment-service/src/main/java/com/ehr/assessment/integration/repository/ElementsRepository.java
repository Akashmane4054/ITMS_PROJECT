package com.ehr.assessment.integration.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.Elements;
import com.ehr.core.dto.RawTypeFieldDTO;

@Repository
public interface ElementsRepository extends JpaRepository<Elements, Long> {

	@Modifying
	@Query("delete from Elements s where s.sectionId=?1 and s.id not in (?2)")
	void deleteBySection(Long sectionId, List<Long> ids);

	@Modifying
	@Query("delete from Elements s where s.sectionId=?1")
	void deleteBySectionId(Long sectionId);

	List<Elements> findByTemplateId(Long id);

	@Query("SELECT se.id FROM Elements se where se.templateId=?1")
	List<Long[]> getAllElementIdsBy(Long templateId);

	@Query("SELECT s FROM Elements s WHERE s.id = :id AND s.sectionId = :sectionId AND s.active = :active")
	Elements findByIdAndSectionIdAndActive(@Param("id") Long id, @Param("sectionId") Long sectionId,
			@Param("active") Boolean active);

	public Elements findByIdAndActive(Long id, Boolean active);

	@Query("select se from Elements se where se.sectionId=?1 and se.active=?2 order by sequence")
	public List<Elements> findBySectionIdAndActive(Long sectionId, Boolean active);

//	@Query("SELECT se.id FROM Elements se where se.companyId=?1")
//	List<Long> getAllIdsByCompanyId(Long companyId);

	List<Elements> findByTemplateIdAndCreatedBy(Long templateId, Long userId);

	List<Elements> findBySectionId(Long sectionId);

	void deleteAllByIdIn(Set<Long> ids);

	@Query("SELECT sea FROM Elements sea " + "WHERE sea.id = :id " + "AND sea.templateId = :templateId "
			+ "ORDER BY sea.createdOn DESC")
	Elements findLatestByElementIdAndTemplateId(@Param("id") Long id, @Param("templateId") Long templateId);

//	List<Elements> findByCompanyId(Long companyId);

	Elements findByIdAndTemplateId(Long id, Long templateId);

	@Query("SELECT COUNT(e) FROM Elements e WHERE e.sectionId = :sectionId AND e.templateId = :templateId AND e.active = true")
	Long countByTemplateIdAndSectionIdAndActiveTrue(@Param("templateId") Long templateId,
			@Param("sectionId") Long sectionId);

	@Query("SELECT new com.ehr.core.dto.RawTypeFieldDTO(e.id, e.sectionId) "
			+ "FROM Elements e WHERE e.sectionId in (?1) AND e.active = TRUE")
	List<RawTypeFieldDTO<Long, Long, String>> findActiveIdAndSectionIdBySectionIdsIn(List<Long> sectionIds);

	@Query("SELECT new com.ehr.core.dto.RawTypeFieldDTO(e.id, e.sectionId, e.label) "
			+ "FROM Elements e WHERE e.sectionId in (?1) AND e.active = TRUE")
	List<RawTypeFieldDTO<Long, Long, String>> findActiveIdAndSectionIdAndLabelBySectionIdsIn(List<Long> sectionIds);
}
