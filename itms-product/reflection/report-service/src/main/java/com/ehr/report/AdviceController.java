package com.ehr.report;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ehr.core.dto.ErrorResponse;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;
import com.ehr.core.util.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class AdviceController extends ResponseEntityExceptionHandler {

	private static final String DETAILED_EXCEPTION = "Detailed Exception!";

	@ExceptionHandler(BussinessException.class)
	public ResponseEntity<Object> handleBussinessException(BussinessException e) {
		log.error(DETAILED_EXCEPTION, e);
		ErrorResponse error = new ErrorResponse(e.getErrorcode().toString(), e.getDescription(), e.getParameter(),
				BussinessException.class.getSimpleName());
		Map<String, Object> map = new HashMap<>();
		map.put(Constants.ERROR, error);
		map.put(Constants.SUCCESS, null);
		return new ResponseEntity<>(map, e.getErrorcode());
	}

	@ExceptionHandler(ContractException.class)
	public ResponseEntity<Object> handleContractException(ContractException e) {
		log.error(DETAILED_EXCEPTION, e);
		ErrorResponse error = new ErrorResponse(e.getErrorcode().toString(), e.getDescription(), e.getParameter(),
				ContractException.class.getSimpleName());
		Map<String, Object> map = new HashMap<>();
		map.put(Constants.ERROR, error);
		map.put(Constants.SUCCESS, null);
		return new ResponseEntity<>(map, e.getErrorcode());

	}

	@ExceptionHandler(TechnicalException.class)
	public ResponseEntity<Object> handleTechnicalException(TechnicalException e) {
		log.error(DETAILED_EXCEPTION, e);
		ErrorResponse error = new ErrorResponse(e.getErrorcode().toString(), e.getDescription(), e.getParameter(),
				TechnicalException.class.getSimpleName());
		Map<String, Object> map = new HashMap<>();
		map.put(Constants.ERROR, error);
		map.put(Constants.SUCCESS, null);
		return new ResponseEntity<>(map, e.getErrorcode());
	}

}
