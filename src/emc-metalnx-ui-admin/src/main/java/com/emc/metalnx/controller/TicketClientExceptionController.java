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

import com.emc.metalnx.core.domain.exceptions.DataGridMissingPathOnTicketException;
import com.emc.metalnx.core.domain.exceptions.DataGridMissingTicketString;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketFileNotFound;
import com.emc.metalnx.services.interfaces.TicketClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@ControllerAdvice(assignableTypes = {TicketClientController.class})
public class TicketClientExceptionController {
    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketClientService ticketClientService;

	@ExceptionHandler({DataGridMissingTicketString.class, DataGridMissingPathOnTicketException.class,
            DataGridTicketFileNotFound.class, IOException.class})
	public ModelAndView handleTicketFileNotFound(DataGridTicketFileNotFound fileNotFound) {
        logger.error("Ticket - file not found");
        ticketClientService.deleteTempTicketDir();
        String path = fileNotFound.getPath();
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", true);
        mav.addObject("objName", path.substring(0, path.lastIndexOf("/")));
        mav.addObject("path", path);
        mav.addObject("ticketString", fileNotFound.getTicketString());
        mav.setViewName("tickets/ticketclient");
		return mav;
	}
}
