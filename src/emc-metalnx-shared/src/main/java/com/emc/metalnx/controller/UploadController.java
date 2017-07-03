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

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridReplicateException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;
import com.emc.metalnx.services.interfaces.UploadService;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.servlet.http.HttpServletRequest;

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

    /**
     * Sets the HTTP response text and status for an upload request.
     *
     * @param uploadMessage string that identifies what kind of uploadMessage happened during an upload
     * @return status OK and response text "Upload Successful" if the upload has no errors. Otherwise, it returns an
     * HTTP status 500 and response text with the corresponding uploadMessage.
     */
    private ResponseEntity<?> getUploadResponse(String uploadMessage, String errorType) {
        HttpStatus status = HttpStatus.OK;
        String path = cc.getCurrentPath();

        JSONObject jsonUploadMsg = new JSONObject();

        try {
            jsonUploadMsg.put("path", path);
            jsonUploadMsg.put("msg", uploadMessage);

            if (!errorType.isEmpty()) {
                jsonUploadMsg.put("errorType", errorType);
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        } catch (JSONException e) {
            logger.error("Could not create JSON object for upload response: {]", e.getMessage());
        }

        return new ResponseEntity<>(jsonUploadMsg.toString(), status);
    }


    @RequestMapping(value = "/", method = RequestMethod.POST, produces = {"text/plain"})
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> upload(HttpServletRequest request) throws DataGridConnectionRefusedException {

        logger.info("Uploading files ...");
        String uploadMessage = "File Uploaded. ";
        String errorType = "";

        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.debug("Request is not a multipart request.");
            uploadMessage = "Request is not a multipart request.";
            errorType = FATAL;
            return getUploadResponse(uploadMessage, errorType);
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("file");

        boolean checksum = Boolean.parseBoolean(multipartRequest.getParameter("checksum"));
        boolean replica = Boolean.parseBoolean(multipartRequest.getParameter("replica"));
        boolean overwrite = Boolean.parseBoolean(multipartRequest.getParameter("overwriteDuplicateFiles"));
        String resources = multipartRequest.getParameter("resources");
        String resourcesToUpload = multipartRequest.getParameter("resourcesToUpload");
        String destPath = multipartRequest.getParameter("uploadDestinationPath");

        try {
            us.upload(multipartFile, destPath, checksum, replica, resources, resourcesToUpload, overwrite);
        } catch (DataGridReplicateException e) {
            uploadMessage += e.getMessage();
            errorType = WARNING;
        } catch (DataGridRuleException e) {
            uploadMessage += METADATA_EXTRACTION_FAILED_MSG;
            errorType = WARNING;
        } catch (DataGridException e) {
            uploadMessage = e.getMessage();
            errorType = FATAL;
        }

        return getUploadResponse(uploadMessage, errorType);
    }
}
