package com.ehr.assessment.business.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElementMasterValueDto {

    private String label;

    private String value;

    private Boolean selected = false;
    
    private String optionWeightage;
    

}
