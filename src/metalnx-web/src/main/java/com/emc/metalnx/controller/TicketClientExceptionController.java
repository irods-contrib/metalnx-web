 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketDownloadException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketInvalidUserException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketUploadException;
import com.emc.metalnx.services.auth.UserTokenDetails;
import com.emc.metalnx.services.interfaces.TicketClientService;

@ControllerAdvice(assignableTypes = {TicketClientController.class})
public class TicketClientExceptionController {
    private static final String IRODS_PATH_SEPARATOR = "/";
    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketClientService ticketClientService;

    @Autowired
    private LoggedUserUtils loggedUserUtils;

	@ExceptionHandler({DataGridTicketDownloadException.class})
	public ModelAndView handleTicketFileNotFound(DataGridTicketDownloadException e) {
        ticketClientService.deleteTempTicketDir();
        String path = e.getPath();
        String objName = path.substring(path.lastIndexOf(IRODS_PATH_SEPARATOR) + 1, path.length());
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", e.getMessage());
        mav.addObject("objName", objName);
        mav.addObject("path", path);
        mav.addObject("ticketString", e.getTicketString());


        String viewName = "tickets/ticketclient";

        DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
        if (loggedUser != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserTokenDetails userTokenDetails = (UserTokenDetails) auth.getDetails();
            mav.addObject("userDetails", userTokenDetails.getUser());
            viewName = "tickets/ticketAuthAccess";
        }

        mav.setViewName(viewName);
		return mav;
	}

	@ExceptionHandler({DataGridTicketInvalidUserException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleTicketInvalidUserException() {
        return "tickets/ticketinvaliduser";
    }

    @ExceptionHandler({DataGridTicketUploadException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public String handleTicketFileUploadError(DataGridTicketUploadException e) {
        ticketClientService.deleteTempTicketDir();
        return e.getMessage();
    }
}
