package com.itms.common.service;

import java.util.Map;

import com.itms.common.domain.EmployeeMaster;
import com.itms.core.exception.BussinessException;
import com.itms.core.exception.ContractException;
import com.itms.core.exception.TechnicalException;

import jakarta.servlet.http.HttpServletRequest;

public interface LoginService {

	Map<String, Object> authenticateUserWithLoginId(EmployeeMaster employeeMaster,
			HttpServletRequest httpServletRequest) throws TechnicalException, BussinessException, ContractException;

}
