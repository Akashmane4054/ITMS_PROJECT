package com.ehr.assessment.integration.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.ElementOptions;
import com.ehr.core.dto.IdAndNameDTO;

@Repository
public interface ElementOptionsRepository extends JpaRepository<ElementOptions, Long> {

	@Modifying
	@Query("delete from ElementOptions s where s.elementId=?1")
	void deleteByElementId(Long elementId);

	List<ElementOptions> findByElementId(Long elementId);

	Optional<ElementOptions> findById(Long elementId);

	List<ElementOptions> findByElementIdAndValue(Long elementId, String value);

	ElementOptions findByValueAndElementId(String value, Long elementId);

	ElementOptions findByElementIdAndId(Long elementId, Long Id);

	@Query("SELECT new com.ehr.core.dto.IdAndNameDTO(e.elementId, e.label) "
			+ "FROM ElementOptions e WHERE e.elementId IN (?1) AND e.value IN (?2)")
	List<IdAndNameDTO> findIdAndLabelByElementIdsInAndValuesIn(Set<Long> elementIds, List<String> values);
}
