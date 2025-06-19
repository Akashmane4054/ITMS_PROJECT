package com.itms.common.serviceImpl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.itms.common.domain.EmployeeMaster;
import com.itms.common.service.LoginService;
import com.itms.core.exception.BussinessException;
import com.itms.core.exception.ContractException;
import com.itms.core.exception.TechnicalException;
import com.itms.core.util.Constants;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

	private static final String CLASSNAME = LoginServiceImpl.class.getSimpleName();

	@Override
	public Map<String, Object> authenticateUserWithLoginId(EmployeeMaster employeeMaster,
			HttpServletRequest httpServletRequest) throws TechnicalException, BussinessException, ContractException {

//		log.info(LogUtil.startLog(CLASSNAME));
		Map<String, Object> map = new HashMap<>();

		try {

			map.put(Constants.SUCCESS, "User Authenticated Successfully");
			map.put(Constants.ERROR, null);
		} catch (Exception e) {
//			log.error(LogUtil.errorLog(e));
			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
//		log.info(LogUtil.exitLog(CLASSNAME));
		return map;
	}

}
