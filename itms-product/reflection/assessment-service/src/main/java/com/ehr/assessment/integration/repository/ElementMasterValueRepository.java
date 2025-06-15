package com.ehr.assessment.integration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.ElementMasterValue;

@Repository
public interface ElementMasterValueRepository extends JpaRepository<ElementMasterValue, Long> {
	
	@Modifying
	@Query("delete from ElementMasterValue s where s.elementId=?1")
	void deleteByElementId(Long elementId);


	List<ElementMasterValue> findByElementId(Long elementId);

	Optional<ElementMasterValue> findById(Long elementId);

	List<ElementMasterValue> findByElementIdAndValue(Long elementId, String value);

	ElementMasterValue findByValueAndElementId(String value, Long elementId);

}
