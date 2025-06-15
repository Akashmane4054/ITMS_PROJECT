package com.ehr.assessment.integration.domain;

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

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(indexes = {@Index(name = "elementwiserating_index_1", columnList = "sectionId,active")})
public class ElementWiseRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sectionId;
    private Long templateId;
    private Long assessmentId;
    private Long userId;
    private Double elementAverage;
    @Column(length = 1000)
    private String elementAnswer;
    private Date createdOn;
    private Date modifiedOn;
    private Boolean active = true;

}
