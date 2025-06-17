package com.itms.user.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.itms.core.exception.BussinessException;
import com.itms.core.exception.ContractException;
import com.itms.core.exception.TechnicalException;
import com.itms.core.util.LogUtil;
import com.itms.user.domain.EmployeeMaster;
import com.itms.user.service.LoginService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
public class loginController {

	@Autowired
	private LoginService loginService;

	@PostMapping(value = "/itmsLogin")
	public Map<String, Object> itmsLogin(@RequestBody EmployeeMaster employeeMaster)
			throws TechnicalException, BussinessException, ContractException {
		log.info(LogUtil.presentationLogger("", "/itmsLogin"));
		return loginService.itmsLogin(employeeMaster);
	}

}
