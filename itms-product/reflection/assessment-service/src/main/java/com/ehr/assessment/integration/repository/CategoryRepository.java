package com.ehr.assessment.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ehr.assessment.integration.domain.CategoryMaster;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryMaster, Long> {


    CategoryMaster findByIdAndActive(Long id, Boolean active);

    CategoryMaster findByName(String name);

    List<CategoryMaster> findByActiveAndId(Boolean active, Long id);

    List<CategoryMaster> findByActive(boolean active);
    
    @Query("SELECT c.name FROM CategoryMaster c WHERE c.id IN :ids")
    List<String> findNamesByIds(@Param("ids") List<Long> ids);
}
