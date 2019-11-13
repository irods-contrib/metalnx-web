 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.controller;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.net.UnknownHostException;

@ControllerAdvice
public class ExceptionController {
    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

	@ExceptionHandler(DataGridConnectionRefusedException.class)
	public String handleConnectionRefusedException(HttpServletResponse response) {
        logger.error("Connection refused");
		response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
		return "errors/serverNotResponding";
	}

    @ExceptionHandler(DataGridTicketException.class)
    public ResponseEntity<String> handleticketException(DataGridTicketException e) {
        logger.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataGridTicketNotFoundException.class)
    public ResponseEntity<String> handleTicketNotFoundException() {
        logger.error("Ticket not found");
        return new ResponseEntity<>("Ticket not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({UnknownHostException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleTicketUnknownHostError() {
        logger.error("Unknown ticket host");
    }
}
