package com.itms.core.exception;

import javax.naming.Binding;

import org.springframework.http.HttpStatus;

public class ContractException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private HttpStatus errorCode;
	private String description;
	private Binding bindingResult;
	private String parameter;

	public ContractException(Binding bindingResult) {
		super();
		this.bindingResult = bindingResult;
	}

	public ContractException(HttpStatus errorCode, String description) {
		super(description);
		this.errorCode = errorCode;
		this.description = description;
	}

	public ContractException(HttpStatus errorCode, String description, String parameter) {
		super(description);
		this.errorCode = errorCode;
		this.description = description;
		this.parameter = parameter;
	}

	public HttpStatus getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(HttpStatus errorCode) {
		this.errorCode = errorCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Binding getBindingResult() {
		return bindingResult;
	}

	public void setBindingResult(Binding bindingResult) {
		this.bindingResult = bindingResult;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
}
