package com.ehr.assessment.integration.domain;

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
@Table(indexes = {@Index(name = "sectionwiseriskmatrix_index_1", columnList = "sectionId,active")})
public class SectionWiseRiskMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long riskRatingId;

    Long sectionId;
    String riskLabel;
    Long riskScore;
    String riskColour;
    String riskComments;
    Long assessmentId = null;  //(Not Null -> Assessment specific, Null -> DEFAULT)
    private Long createdBy;
    private Long modifiedBy;
    private Date createdOn;
    private Date modifiedOn;
    private Boolean active = true;

}
