 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketDownloadException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketFileNotFoundException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketInvalidUserException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketUploadException;
import com.emc.metalnx.services.interfaces.TicketClientService;

/**
 * Controller that will handle anonymous access to collections and data objects
 * using tickets.
 */
@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/ticketclient")
public class TicketClientController {
	private static final String APPLICATION_OCTET_STREAM = "text/octet-stream";
	private static final String HEADER_FORMAT = "attachment;filename=\"%s\"";
	private static final Logger logger = LoggerFactory.getLogger(TicketClientController.class);
	private static final String IRODS_PATH_SEPARATOR = "/";
	private static final String CONTENT_DISPOSITION = "Content-Disposition";

	@Autowired
	private TicketClientService ticketClientService;

	@Autowired
	private LoggedUserUtils loggedUserUtils;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index(final Model model, @RequestParam("ticketstring") final String ticketString,
			@RequestParam("ticketpath") final String path) throws DataGridConnectionRefusedException {
		logger.info("Accessing ticket {} on {}", ticketString, path);
		String objName = path.substring(path.lastIndexOf(IRODS_PATH_SEPARATOR) + 1, path.length());
		model.addAttribute("objName", objName);
		model.addAttribute("ticketString", ticketString);
		model.addAttribute("path", path);

		DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();

		return loggedUser == null ? "tickets/ticketclient" : "tickets/ticketAuthAccess";
	}

	@RequestMapping(value = "/invaliduser", method = RequestMethod.GET)
	public String invalidUser() {
		return "tickets/ticketinvaliduser";
	}

	@RequestMapping(value = "/{ticketstring}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void upload(@PathVariable("ticketstring") final String ticketString, final HttpServletRequest request)
			throws DataGridConnectionRefusedException, DataGridTicketUploadException, IOException,
			DataGridTicketInvalidUserException {
		logger.info("Uploading files using ticket: {}", ticketString);

		if (!(request instanceof MultipartHttpServletRequest)) {
			logger.error("Request is not a multipart request. Stop.");
			return;
		}

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile multipartFile = multipartRequest.getFile("file");
		String destPath = multipartRequest.getParameter("path");

		File file = multipartToFile(multipartFile);
		ticketClientService.transferFileToIRODSUsingTicket(ticketString, file, destPath);
	}

	@RequestMapping(value = "/{ticketstring}", method = RequestMethod.GET)
	public void download(@PathVariable("ticketstring") final String ticketString,
			@RequestParam("path") final String path, final HttpServletResponse response)
			throws DataGridConnectionRefusedException, DataGridTicketFileNotFoundException, IOException,
			DataGridTicketInvalidUserException, DataGridTicketDownloadException {
		logger.info("Getting files using ticket: {}", ticketString);

		File file = ticketClientService.getFileFromIRODSUsingTicket(ticketString, path);

		if (file != null) {
			setResponseStream(response, file);
		}

		ticketClientService.deleteTempTicketDir();
	}

	/**
	 * Sets the HTTP response stream.
	 * 
	 * @param response
	 *            HTTP response
	 * @param file
	 *            file to be sent back with the HTTP response
	 * @throws IOException
	 *             if Metalnx cannot stream the file
	 */
	private void setResponseStream(final HttpServletResponse response, final File file) throws IOException {
		String filename = file.getName();
		response.setContentType(APPLICATION_OCTET_STREAM);
		response.setHeader(CONTENT_DISPOSITION, String.format(HEADER_FORMAT, filename));
		FileCopyUtils.copy(getInputStream(file), response.getOutputStream()); // takes care of closing streams
	}

	/**
	 * Gets an input stream from a file within a directory
	 * 
	 * @param file
	 *            file to get the stream from
	 * @return stream for the file
	 * @throws FileNotFoundException
	 */
	private InputStream getInputStream(final File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}

	/**
	 * Converts a multipart file comming from an HTTP request into a File instance.
	 * 
	 * @param multipartFile
	 *            file uploaded
	 * @return File instance
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	private File multipartToFile(final MultipartFile multipartFile) throws IllegalStateException, IOException {
		File convFile = new File(multipartFile.getOriginalFilename());
		multipartFile.transferTo(convFile);
		return convFile;
	}
}
