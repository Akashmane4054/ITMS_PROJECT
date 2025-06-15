package com.ehr.assessment.integration.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Here Assessments Tabs or Sections are stored (Survey Pages)
 *  
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(indexes = {@Index(name = "section_index_1",  columnList="templateId,active")})
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    private Long templateId;

    private Long sequence;
    
    private Long createdBy;

    private Date createdOn;

    private Long modifiedBy;

    private Date modifiedOn;

    private Boolean active = true;
    
}
