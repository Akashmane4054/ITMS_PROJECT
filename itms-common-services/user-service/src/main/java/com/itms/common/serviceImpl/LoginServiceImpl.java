package com.itms.common.serviceImpl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.itms.common.domain.EmployeeMaster;
import com.itms.common.service.LoginService;
import com.itms.core.exception.BussinessException;
import com.itms.core.exception.ContractException;
import com.itms.core.exception.TechnicalException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

	@Override
	public Map<String, Object> itmsLogin(EmployeeMaster employeeMaster)
			throws TechnicalException, BussinessException, ContractException {

		return null;
	}

}
