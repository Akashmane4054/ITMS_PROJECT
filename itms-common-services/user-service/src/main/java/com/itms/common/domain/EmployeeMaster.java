package com.itms.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_master")
public class EmployeeMaster {

    @Id()
    @Column(name = "emp_id")
    private String empId;

    @Column(name = "emp_name")
    private String empName;

    private String password;
    private String employeeOf;
    private int moduleCode;
    private int roleId;
    private int empStatus;
    private int stateCode;
    private int bankCode;
    private String emailId;
    private int loginCount;
    private String gender;
    private String email2;
    private int bDay;
    private int bMonth;
    private int bYear;
    private int jDay;
    private int jMonth;
    private int jYear;
    private String mobile;
    private String extension;
    private String blood;
    private String city;
    private String state;
    private String sq;
    private String sa;
    private String designation;
    private String project;
    private String bank;
    private String alternateNo;
    private String tlName;
}