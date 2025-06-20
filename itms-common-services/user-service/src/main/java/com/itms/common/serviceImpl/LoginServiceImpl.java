package com.itms.common.serviceImpl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itms.common.domain.EmployeeMaster;
import com.itms.common.repository.EmployeeMasterRepository;
import com.itms.common.service.LoginService;
import com.itms.core.exception.BussinessException;
import com.itms.core.exception.ContractException;
import com.itms.core.exception.TechnicalException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

	@Autowired
	private EmployeeMasterRepository employeeMasterRepository;

	private static final String CLASSNAME = LoginServiceImpl.class.getSimpleName();

	@Override
	public Map<String, Object> authenticateUserWithLoginId(EmployeeMaster employeeMaster,
			HttpServletRequest httpServletRequest) throws TechnicalException, BussinessException, ContractException {

		Map<String, Object> responseMap = new HashMap<>();

//		try {
//			// Fetch employee from DB by loginId
//			EmployeeMaster optionalEmployee = employeeMasterRepository
//					.findByEmpId(employeeMaster.);
//
//			if (optionalEmployee.isEmpty()) {
//				throw new BussinessException(HttpStatus.UNAUTHORIZED, "Invalid login ID");
//			}
//
//			EmployeeMaster dbEmployee = optionalEmployee.get();
//
//			// Validate password
//			if (!passwordEncoder.matches(employeeMaster.getPassword(), dbEmployee.getPassword())) {
//				throw new BussinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
//			}
//
//			// Generate JWT token
//			String token = jwtTokenUtil.generateToken(dbEmployee.getLoginId());
//
//			// Prepare response map
//			responseMap.put(Constants.SUCCESS, "User Authenticated Successfully");
//			responseMap.put("token", token);
//			responseMap.put("employeeId", dbEmployee.getEmpId());
//			responseMap.put("roleId", dbEmployee.getRoleId());
//			responseMap.put(Constants.ERROR, null);
//		} catch (Exception e) {
////	        log.error(LogUtil.errorLog(e));
//			throw new TechnicalException(Constants.TECHNICAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
//		}

//	    log.info(LogUtil.exitLog(CLASSNAME));
		return responseMap;
	}

}
