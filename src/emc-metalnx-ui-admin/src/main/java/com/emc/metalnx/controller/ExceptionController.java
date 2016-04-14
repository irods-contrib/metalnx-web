package com.emc.metalnx.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

@ControllerAdvice
public class ExceptionController {

	@ExceptionHandler(DataGridConnectionRefusedException.class)
	public String handleConnectionRefusedException(HttpServletResponse response) {	
		response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
		
		return "errors/serverNotResponding";
	}
}
