package com.itms.user.serviceImpl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.itms.core.exception.BussinessException;
import com.itms.core.exception.ContractException;
import com.itms.core.exception.TechnicalException;
import com.itms.user.domain.EmployeeMaster;
import com.itms.user.service.LoginService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

	@Override
	public Map<String, Object> itmsLogin(EmployeeMaster employeeMaster)
			throws TechnicalException, BussinessException, ContractException {
		// TODO Auto-generated method stub
		return null;
	}

}
