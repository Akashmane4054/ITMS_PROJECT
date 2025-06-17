package com.itms.user.service;

import java.util.Map;

import com.itms.core.exception.BussinessException;
import com.itms.core.exception.ContractException;
import com.itms.core.exception.TechnicalException;
import com.itms.user.domain.EmployeeMaster;

public interface LoginService {

	Map<String, Object> itmsLogin(EmployeeMaster employeeMaster)
			throws TechnicalException, BussinessException, ContractException;

}
