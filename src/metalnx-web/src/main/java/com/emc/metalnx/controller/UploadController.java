 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.controller;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridReplicateException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;
import com.emc.metalnx.services.interfaces.RuleDeploymentService;
import com.emc.metalnx.services.interfaces.UploadService;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/upload")
public class UploadController {

	private static final String WARNING = "warning";
	private static final String FATAL = "fatal";
	private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
	public static final String METADATA_EXTRACTION_FAILED_MSG = "Metadata extraction failed.";

	@Autowired
	private CollectionController cc;

	@Autowired
	private UploadService us;

	@Autowired
	private RuleDeploymentService ruleDeploymentService;

	@Value("${default.storage.resource}")
	private String defaultStorageResc;

	/**
	 * Sets the HTTP response text and status for an upload request.
	 *
	 * @param uploadMessage
	 *            string that identifies what kind of uploadMessage happened during
	 *            an upload
	 * @return status OK and response text "Upload Successful" if the upload has no
	 *         errors. Otherwise, it returns an HTTP status 500 and response text
	 *         with the corresponding uploadMessage.
	 */
	private ResponseEntity<?> getUploadResponse(final String uploadMessage, final String errorType) {
		logger.info("getUploadResponse()");
		logger.info("uploadMessage:{}", uploadMessage);
		logger.info("errorType:{}", errorType);

		HttpStatus status = HttpStatus.OK;
		String path = cc.getCurrentPath();

		JSONObject jsonUploadMsg = new JSONObject();

		try {
			jsonUploadMsg.put("path", path);
			jsonUploadMsg.put("msg", uploadMessage);

			if (!errorType.isEmpty()) {
				jsonUploadMsg.put("errorType", errorType);
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				logger.warn("passing along internal server error:{}", errorType);
			}
		} catch (JSONException e) {
			logger.error("Could not create JSON object for upload response: {]", e.getMessage());
		}

		return new ResponseEntity<>(jsonUploadMsg.toString(), status);
	}

	@RequestMapping(value = "/", method = RequestMethod.POST, produces = { "text/plain" })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<?> upload(final HttpServletRequest request) throws DataGridException {

		logger.info("upload()");
		String uploadMessage = "File Uploaded. ";
		String errorType = "";

		if (!(request instanceof MultipartHttpServletRequest)) {
			logger.warn("Request is not a multipart request.");
			uploadMessage = "Request is not a multipart request.";
			errorType = FATAL;
			return getUploadResponse(uploadMessage, errorType);
		}

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile multipartFile = multipartRequest.getFile("file");
		logger.info("multipartFile:{}", multipartFile);
		boolean isRuleDeployment = Boolean.parseBoolean(multipartRequest.getParameter("ruleDeployment"));
		boolean checksum = Boolean.parseBoolean(multipartRequest.getParameter("checksum"));
		boolean replica = Boolean.parseBoolean(multipartRequest.getParameter("replica"));
		boolean overwrite = Boolean.parseBoolean(multipartRequest.getParameter("overwriteDuplicateFiles"));
		String resources = multipartRequest.getParameter("resources");
		String resourcesToUpload = multipartRequest.getParameter("resourcesToUpload");
		String destPath = multipartRequest.getParameter("uploadDestinationPath");

		if ("undefined".equalsIgnoreCase(resourcesToUpload)) {
			if (defaultStorageResc == null || defaultStorageResc.isEmpty()) {
				resourcesToUpload = null;
			} else {
				resourcesToUpload = defaultStorageResc;
			}
		}

		logger.info("parsed parameters...");

		try {
			if (isRuleDeployment) {
				ruleDeploymentService.deployRule(multipartFile);
			} else {
				us.upload(multipartFile, destPath, checksum, replica, resources, resourcesToUpload, overwrite);
			}
		} catch (DataGridReplicateException e) {
			uploadMessage += e.getMessage();
			errorType = WARNING;
			logger.warn("DataGridReplicateException during upload, will pass back as a warning", e);
		} catch (DataGridRuleException e) {
			uploadMessage += METADATA_EXTRACTION_FAILED_MSG;
			errorType = WARNING;
			logger.warn("DataGridRule exception extracting metadata, will pass back as warning", e);
		} catch (DataGridException e) {
			uploadMessage = e.getMessage();
			errorType = FATAL;
			logger.error("DataGridException uploading file", e);
			logger.warn("DataGridException uplaoding file, will pass back as warning", e);
			// throw e;
		} catch (JargonException e) {
			uploadMessage = e.getMessage();
			errorType = FATAL;
			logger.error("JargonException uploading file", e);
			logger.warn("JargonException uplaoding file, will pass back as warning", e);
		} catch (Throwable t) {
			logger.error("unexpected exception in upload", t);
			errorType = FATAL;
			throw t;
		}

		return getUploadResponse(uploadMessage, errorType);
	}
}
