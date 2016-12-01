/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.controller;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridReplicateException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;
import com.emc.metalnx.service.utils.DataGridFileForUpload;
import com.emc.metalnx.services.interfaces.UploadService;
import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/upload")
public class UploadController {

    private static final String WARNING = "warning";
    private static final String FATAL = "fatal";
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private CollectionController cc;

    @Autowired
    private UploadService us;

    // contains the file name, DataGridForUpload map containing all files that will be uploaded
    private Map<String, DataGridFileForUpload> filesForUploadMap;

    @PostConstruct
    public void init() {
        filesForUploadMap = new HashMap<>();
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, produces = {"text/plain"})
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

        String filename = getFileName(request);

        logger.debug("Uploading file {} ", filename);

        DataGridFileForUpload file;

        synchronized (filesForUploadMap) {
            file = filesForUploadMap.get(filename);
        }

        try {
            file.writeChunk(us.getChunk(request));

            if (file.isFileReadyForDataGrid()) {
                us.transferFileToDataGrid(file);
                synchronized (filesForUploadMap) {
                    filesForUploadMap.remove(file.getFileName());
                }
            }
        } catch (DataGridReplicateException | DataGridRuleException e) {
            uploadMessage += e.getMessage();
            errorType = WARNING;
        } catch (DataGridException e) {
            uploadMessage = e.getMessage();
            errorType = FATAL;
        }

        if (!errorType.isEmpty()) {
            File tmpSessionDir = new File(file.getUser(), file.getPathToParts());
            FileUtils.deleteQuietly(tmpSessionDir);
        }

        return getUploadResponse(uploadMessage, errorType);
    }

    @RequestMapping(value = "/prepare/", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void prepareFilesForUpload(HttpServletRequest request) {
        logger.debug("Preparing file for upload.");

        DataGridFileForUpload file;
        try {
            file = us.buildFileForUpload(request);
            synchronized (filesForUploadMap) {
                filesForUploadMap.put(file.getFileName(), file);
            }
        } catch (DataGridException e) {
        }
    }

    @RequestMapping(value = "/resume/", method = RequestMethod.POST, produces = {"text/plain"})
    @ResponseBody
    public String resumeUpload(HttpServletResponse response, @RequestParam("fileName") String fileName) {

        JSONObject resumeUploadJSON = new JSONObject();
        JSONArray listOfChunksUploadedJSON;
        DataGridFileForUpload resumeFileUpload;

        try {
            synchronized (filesForUploadMap) {
                resumeFileUpload = filesForUploadMap.get(fileName);
            }
            resumeUploadJSON.append("fileName", resumeFileUpload.getFileName());
            resumeUploadJSON.append("lastPartUploaded", resumeFileUpload.getLastPartUploaded());

            listOfChunksUploadedJSON = new JSONArray();
            List<Integer> chunks = resumeFileUpload.getChunksUploadedFromLastPartUploaded();
            for (Integer chunk : chunks) {
                listOfChunksUploadedJSON.put(chunk);
            }

            resumeUploadJSON.append("listOfChunksUploaded", listOfChunksUploadedJSON);
        } catch (JSONException | NullPointerException e) {
            logger.error("Could not resume upload for file: {} {}", fileName, e.getMessage());
        }

        return resumeUploadJSON.toString();
    }

    /**
     * Gets the name of a file being transferred from an HTTP request
     *
     * @param request HTTP request for file transfer
     * @return string representing the file's name
     */
    private String getFileName(HttpServletRequest request) {
        String[] fileName = (String[]) request.getParameterMap().get("fileName");
        return fileName[0];
    }

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
}
