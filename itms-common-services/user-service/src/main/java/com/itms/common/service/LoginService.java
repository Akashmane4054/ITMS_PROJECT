package com.itms.common.service;

import java.util.Map;

import com.itms.common.domain.EmployeeMaster;
import com.itms.core.exception.BussinessException;
import com.itms.core.exception.ContractException;
import com.itms.core.exception.TechnicalException;

public interface LoginService {

	Map<String, Object> itmsLogin(EmployeeMaster employeeMaster)
			throws TechnicalException, BussinessException, ContractException;

}
