/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.controller;

import com.emc.metalnx.core.domain.exceptions.DataGridTicketDownloadException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketInvalidUserException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketUploadException;
import com.emc.metalnx.services.interfaces.TicketClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(assignableTypes = {TicketClientController.class})
public class TicketClientExceptionController {
    private static final String IRODS_PATH_SEPARATOR = "/";
    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketClientService ticketClientService;

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
        mav.setViewName("tickets/ticketclient");
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
