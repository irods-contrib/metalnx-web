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

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.modelattribute.collection.CollectionOrDataObjectForm;
import com.emc.metalnx.service.utils.DataGridFileForUpload;
import com.emc.metalnx.services.exceptions.DataGridCorruptedPartException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.FileOperationService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.UserService;
import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/fileOperation")
public class FileOperationsController {

    @Autowired
    CollectionController collectionController;

    @Autowired
    CollectionService collectionService;

    @Autowired
    IRODSServices irodsServices;

    @Autowired
    UserService userService;

    @Autowired
    FileOperationService fileOperationService;

    @Autowired
    LoggedUserUtils loggedUserUtils;

    @Value("${irods.zoneName}")
    private String zoneName;

    // contains the path to the file that will be downloaded
    private String filePathToDownload;

    // checks if it's necessary to remove any temporary collections created for downloading
    private boolean removeTempCollection;

    // contains the file name, DataGridForUpload map containing all files that will be uploaded
    private Map<String, DataGridFileForUpload> filesForUploadMap;

    private static String TRASH_PATH;

    private static final Logger logger = LoggerFactory.getLogger(FileOperationsController.class);

    @PostConstruct
    public void init() {
        filesForUploadMap = new HashMap<String, DataGridFileForUpload>();
        TRASH_PATH = String.format("/%s/trash/home/%s", irodsServices.getCurrentUserZone(), irodsServices.getCurrentUser());
    }

    @RequestMapping(value = "/move", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String move(Model model, @RequestParam("targetPath") String targetPath) throws DataGridException, JargonException {

        List<String> sourcePaths = collectionController.getSourcePaths();
        List<String> failedMoves = new ArrayList<String>();
        String fileMoved = "";

        try {
            for (String sourcePathItem : sourcePaths) {
                String item = sourcePathItem.substring(sourcePathItem.lastIndexOf("/") + 1, sourcePathItem.length());
                if (!fileOperationService.move(sourcePathItem, targetPath)) {
                    failedMoves.add(item);
                } else if (sourcePaths.size() == 1) {
                    fileMoved = item;
                }
            }
        } catch (DataGridException e) {
            logger.error("Could not move item to {}: {}", targetPath, e.getMessage());
        }

        if (fileMoved != "") {
            model.addAttribute("fileMoved", fileMoved);
        }

        model.addAttribute("failedMoves", failedMoves);
        sourcePaths.clear();

        return collectionController.getSubDirectories(model, targetPath);
    }

    @RequestMapping(value = "/copy", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String copy(Model model, @RequestParam("targetPath") String targetPath) throws DataGridException, JargonException {

        List<String> sourcePaths = collectionController.getSourcePaths();
        List<String> failedCopies = new ArrayList<String>();
        String fileCopied = "";

        for (String sourcePathItem : sourcePaths) {
            String item = sourcePathItem.substring(sourcePathItem.lastIndexOf("/") + 1, sourcePathItem.length());
            if (!fileOperationService.copy(sourcePathItem, targetPath)) {
                failedCopies.add(item);
            } else if (sourcePaths.size() == 1) {
                fileCopied = item;
            }
        }

        if (fileCopied != "") {
            model.addAttribute("fileCopied", fileCopied);
        }

        model.addAttribute("failedCopies", failedCopies);
        sourcePaths.clear();

        return collectionController.getSubDirectories(model, targetPath);
    }

    /**
     * Delete a replica of a data object
     *
     * @param model
     * @param path    path to the parent of the data object to be deleted
     * @param fileName      name of the data object to be deleted
     * @param replicaNumber number of the replica that is going to be deleted
     * @return the template that shows the data object information with the replica table refreshed
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "deleteReplica", method = RequestMethod.POST)
    public String deleteReplica(Model model, @RequestParam("path") String path, @RequestParam("fileName") String fileName,
                                @RequestParam String replicaNumber) throws DataGridConnectionRefusedException {

        boolean inAdminMode = loggedUserUtils.getLoggedDataGridUser().isAdmin();

        if (fileOperationService.deleteReplica(path, fileName, Integer.parseInt(replicaNumber), inAdminMode)) {
            model.addAttribute("delReplReturn", "success");
        } else {
            model.addAttribute("delReplReturn", "failure");
        }
        return collectionController.getFileInfo(model, path);
    }

    @RequestMapping(value = "/replicate", method = RequestMethod.POST)
    public String replicate(Model model, HttpServletRequest request) throws DataGridConnectionRefusedException {

        List<String> sourcePaths = collectionController.getSourcePaths();
        String[] resources = (String[]) request.getParameterMap().get("resourcesForReplication");
        List<String> failedReplicas = new ArrayList<String>();

        if (resources != null) {
            String targetResource = resources[0];
            boolean inAdminMode = loggedUserUtils.getLoggedDataGridUser().isAdmin();
            for (String sourcePathItem : sourcePaths) {
                if (!fileOperationService.replicateDataObject(sourcePathItem, targetResource, inAdminMode)) {
                    String item = sourcePathItem.substring(sourcePathItem.lastIndexOf("/") + 1, sourcePathItem.length());
                    failedReplicas.add(item);
                }
            }
        }

        if (!sourcePaths.isEmpty()) {
            model.addAttribute("failedReplicas", failedReplicas);
            sourcePaths.clear();
        }

        return collectionController.index(model, request, false);
    }

    @RequestMapping(value = "/upload/", method = RequestMethod.POST, produces = {"text/plain"})
    @ResponseBody
    public String upload(Model model, HttpServletRequest request, RedirectAttributes redirectAttr) throws DataGridConnectionRefusedException,
            IOException {
        logger.debug("Uploading files ...");

        boolean isFileTransferringComplete = false;
        String error = "";

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile multipartFileChunk = multipartRequest.getFile("fileChunk");

            String[] chunkNumberParam = (String[]) request.getParameterMap().get("chunkNumber");
            String[] filePartParam = (String[]) request.getParameterMap().get("filePart");
            String[] fileName = (String[]) request.getParameterMap().get("fileName");
            String[] partCRC32Param = (String[]) request.getParameterMap().get("partCRC32");
            DataGridFileForUpload fileForUpload = null;

            logger.debug("Uploading file {} ", fileName[0]);

            synchronized (filesForUploadMap) {
                fileForUpload = filesForUploadMap.get(fileName[0]);
            }

            try {

                int chunkNumber = Integer.valueOf(chunkNumberParam[0]);
                int partNumber = Integer.valueOf(filePartParam[0]);
                long partCRC32 = Long.valueOf(partCRC32Param[0]);

                isFileTransferringComplete = fileForUpload.writeChunkToFile(multipartFileChunk, partNumber, chunkNumber, partCRC32);

                // transfer file to data grid when all its parts were sent to
                // server and there is no parts corrupted
                if (isFileTransferringComplete && !fileForUpload.isFileCorrupted()) {
                    logger.info("File transferring complete. Sending it to the data grid.");
                    fileOperationService.transferFileToDataGrid(fileForUpload);

                    // removing the file uploaded from server map
                    synchronized (filesForUploadMap) {
                        filesForUploadMap.remove(fileForUpload.getFileName());
                    }
                }
            } catch (NullPointerException e) {
                logger.error("Could not upload file. {}", e.getMessage());
                error = ":nullpointer";
            } catch (DataGridCorruptedPartException e) {
                logger.error("Corrupted part in file {}", fileName[0], e.getMessage());
                error = ":corruptedfile";
            } catch (DataGridException e) {
                logger.error("Could not transfer file to IRODS resource. {}", e.getMessage());
                fileOperationService.deleteItem(fileForUpload.getTargetPath() + "/" + fileForUpload.getFileName(), true);
                error = ":transfererror";
            }
            if (!error.isEmpty()) {
                File tmpSessionDir = new File(fileForUpload.getUser(), fileForUpload.getPathToParts());
                FileUtils.deleteQuietly(tmpSessionDir);
            }
        }

        return collectionController.getCurrentPath() + error;
    }

    @RequestMapping(value = "/prepareFilesForUpload", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void prepareFilesForUpload(HttpServletRequest request, @RequestParam("fileName") String fileName, @RequestParam("fileSize") long fileSize,
                                      @RequestParam("partSize") long partSize, @RequestParam("totalParts") int totalParts, @RequestParam("chunkSize") int chunkSize,
                                      @RequestParam("totalChunksPerPart") int totalChunksPerPart, @RequestParam("totalChunks") int totalChunks,
                                      @RequestParam("checksum") boolean checksum, @RequestParam("replica") boolean replica, @RequestParam("resources") String resources,
                                      @RequestParam("resourcesToUpload") String resourcesToUpload, @RequestParam("overwriteDuplicateFiles") boolean overwriteDuplicateFiles)
            throws FileNotFoundException {

        logger.debug("Preparing {} for upload.", fileName);

        String user = irodsServices.getCurrentUser();
        DataGridFileForUpload fileForUpload = new DataGridFileForUpload(fileName, fileSize, partSize, totalParts, chunkSize, totalChunksPerPart,
                totalChunks, resourcesToUpload, collectionController.getCurrentPath(), user);

        fileForUpload.setDataGridComputeChecksum(checksum);
        fileForUpload.setDataGridOverwriteDuplicatedFiles(overwriteDuplicateFiles);
        fileForUpload.setReplicateFile(replica);
        fileForUpload.setDataGridReplicationResource(resources);

        synchronized (filesForUploadMap) {
            filesForUploadMap.put(fileName, fileForUpload);
        }
    }

    @RequestMapping(value = "/resumeUpload/", method = RequestMethod.POST, produces = {"text/plain"})
    @ResponseBody
    public String resumeUpload(HttpServletResponse response, @RequestParam("fileName") String fileName) {

        JSONObject resumeUploadJSON = new JSONObject();
        JSONArray listOfChunksUploadedJSON = null;
        DataGridFileForUpload resumeFileUpload = null;

        try {
            resumeFileUpload = filesForUploadMap.get(fileName);
            resumeUploadJSON.append("fileName", resumeFileUpload.getFileName());
            resumeUploadJSON.append("lastPartUploaded", resumeFileUpload.getLastPartUploaded());

            listOfChunksUploadedJSON = new JSONArray();
            List<Integer> chunks = resumeFileUpload.getChunksUploadedFromLastPartUploaded();
            for (Integer chunk : chunks) {
                listOfChunksUploadedJSON.put(chunk);
            }

            resumeUploadJSON.append("listOfChunksUploaded", listOfChunksUploadedJSON);
        } catch (JSONException e) {
            logger.error("Could not resume upload for file: {} {}", fileName, e.getMessage());
        } catch (NullPointerException e) {
            logger.error("Could not resume upload for file: {} {}", fileName, e.getMessage());
        }

        return resumeUploadJSON.toString();
    }

    @RequestMapping(value = "/prepareFilesForDownload/", method = RequestMethod.GET, produces = {"text/plain"})
    @ResponseBody
    public String prepareFilesForDownload(HttpServletResponse response) throws DataGridConnectionRefusedException {

        List<String> sourcePaths = collectionController.getSourcePaths();

        try {
            // if a single file was selected, it will be transferred directly
            // through the HTTP response
            if (sourcePaths.size() == 1 && collectionService.isDataObject(sourcePaths.get(0))) {
                removeTempCollection = false;
                filePathToDownload = sourcePaths.get(0);
                String permissionType = collectionService.getPermissionsForPath(filePathToDownload);
                if (permissionType.equalsIgnoreCase("none")) {
                    throw new DataGridException("Lack of permission to download file " + filePathToDownload);
                }
            } else {
                filePathToDownload = collectionService.prepareFilesForDownload(sourcePaths);
                removeTempCollection = true;
            }

            sourcePaths.clear();
        } catch (DataGridConnectionRefusedException e) {
            throw e;
        } catch (DataGridException | IOException e) {
            logger.error("Could not download selected items: ", e.getMessage());
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        return "";
    }

    @RequestMapping(value = "/download/", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void download(HttpServletResponse response) throws DataGridConnectionRefusedException {

        try {
            fileOperationService.download(filePathToDownload, response, removeTempCollection);
            filePathToDownload = "";
            removeTempCollection = false;
        } catch (DataGridException | IOException e) {
            logger.error("Could not download selected items: ", e.getMessage());
        }
    }

    @RequestMapping(value = "delete/", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String deleteCollectionAndDataObject(Model model) throws DataGridException, JargonException {
        boolean forceRemove = false;
        List<String> sourcePaths = collectionController.getSourcePaths();
        List<String> failedDeletions = new ArrayList<String>();
        String fileDeleted = null;

        for (String path : sourcePaths) {
            forceRemove = path.startsWith(TRASH_PATH);
            
            if (!fileOperationService.deleteItem(path, forceRemove)) {
                String item = path.substring(path.lastIndexOf("/") + 1, path.length());
                failedDeletions.add(item);
            } 
            else if (sourcePaths.size() == 1) {
                fileDeleted = path.substring(path.lastIndexOf("/") + 1, path.length());
            }
            
            collectionController.removePathFromHistory(path);
        }

        sourcePaths.clear();

        if (fileDeleted != null) {
            model.addAttribute("fileDeleted", fileDeleted);
        }

        model.addAttribute("failedDeletions", failedDeletions);
        model.addAttribute("currentPath", collectionController.getCurrentPath());
        model.addAttribute("parentPath", collectionController.getParentPath());
        
        return collectionController.getSubDirectories(model, collectionController.getCurrentPath());
    }

    @RequestMapping(value = "emptyTrash/", method = RequestMethod.POST)
    public ResponseEntity<String> emptyTrash(Model model) throws DataGridConnectionRefusedException {
        String trashForCurrentPath = collectionService.getTrashForPath(collectionController.getCurrentPath());
        DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
        if (fileOperationService.emptyTrash(loggedUser, trashForCurrentPath)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Displays the modify user form with all fields set to the selected collection
     *
     * @param model
     * @return collectionForm with fields set
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "modify/", method = RequestMethod.GET)
    public String showModifyForm(Model model) throws DataGridException {
        List<String> sourcePaths = collectionController.getSourcePaths();
        String currentPath = collectionController.getCurrentPath();
        String parentPath = collectionController.getParentPath();
        if (sourcePaths.size() != 1) {
            throw new DataGridException("Cannot rename more than one element at a time.");
        }

        String formType = "editDataObjectForm";
        String targetPath = sourcePaths.get(0);
        CollectionOrDataObjectForm targetForm = new CollectionOrDataObjectForm();
        DataGridCollectionAndDataObject dataGridCollectionAndDataObject = collectionService.findByName(targetPath);

        logger.info("Modify form for {}", targetPath);

        targetForm.setCollectionName(dataGridCollectionAndDataObject.getName());
        targetForm.setPath(dataGridCollectionAndDataObject.getPath());
        targetForm.setParentPath(currentPath);

        if (dataGridCollectionAndDataObject.isCollection()) {
            formType = "editCollectionForm";
            targetForm.setCollection(true);
            logger.info("Setting inheritance for {}", targetPath);
            targetForm.setInheritOption(collectionService.getInheritanceOptionForCollection(targetForm.getPath()));
        }

        model.addAttribute("currentPath", currentPath);
        model.addAttribute("parentPath", parentPath);
        model.addAttribute("collection", targetForm);
        model.addAttribute("requestMapping", "/collections/modify/action/");

        return String.format("collections/%s", formType);
    }

}
