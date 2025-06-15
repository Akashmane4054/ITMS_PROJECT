package com.ehr.assessment.integration.domain;

import java.util.Date;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(indexes = {@Index(name = "sectionwiserating_index_1", columnList = "sectionId,active")})
public class SectionWiseRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sectionId;
    private Long templateId;
    private Long assessmentId;
    private Long userId;
    private Double sectionAverage;
    private Double sectionTotal;
    private Long riskRatingId;
    private Date createdOn;
    private Date modifiedOn;
    private Boolean active = true;

}
