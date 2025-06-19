package com.itms.common.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.itms.common.domain.EmployeeMaster;
import com.itms.common.service.LoginService;
import com.itms.core.exception.BussinessException;
import com.itms.core.exception.ContractException;
import com.itms.core.exception.TechnicalException;
import com.itms.core.util.EndPointReference;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
public class loginController {

	@Autowired
	private LoginService loginService;

	@PostMapping(value = { "/loginWithLoginId", EndPointReference.LOGIN_WITH_LOGIN_ID })
	public Map<String, Object> loginWithLoginId(@RequestBody EmployeeMaster employeeMaster,
			HttpServletRequest httpServletRequest) throws TechnicalException, BussinessException, ContractException {
//		log.info(LogUtil.presentationLogger(EndPointReference.LOGIN_WITH_LOGIN_ID));
		return loginService.authenticateUserWithLoginId(employeeMaster, httpServletRequest);
	}
}
