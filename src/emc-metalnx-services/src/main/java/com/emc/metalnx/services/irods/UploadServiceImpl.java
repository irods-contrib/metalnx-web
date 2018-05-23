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

package com.emc.metalnx.services.irods;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.ResourceAO;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridFileAlreadyExistsException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.FileOperationService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.RuleService;
import com.emc.metalnx.services.interfaces.UploadService;
import com.emc.metalnx.services.machine.util.DataGridUtils;

@Service
@Transactional
public class UploadServiceImpl implements UploadService {

	private static final int BUFFER_SIZE = 4 * 1024 * 1024;
	private static final Logger logger = LoggerFactory.getLogger(UploadServiceImpl.class);

	@Autowired
	private RuleService rs;

	@Autowired
	private FileOperationService fos;

	@Autowired
	private IRODSServices is;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private ConfigService configService;

	@Override
	public boolean upload(MultipartFile file, String targetPath, boolean computeCheckSum, boolean replicateFile,
			String replicationResc, String destResc, boolean overwrite) throws DataGridException {

		logger.info("upload()");

		if (file == null || file.isEmpty() || "".equals(targetPath) || targetPath == null || "".equals(destResc)
				|| destResc == null) {
			logger.error("File could not be sent to the data grid.");
			return false;
		}

		logger.info("file:{}", file);
		logger.info("targetPath:{}", targetPath);
		logger.info("computeCheckSum:{}", computeCheckSum);
		logger.info("replicateFile:{}", replicateFile);
		logger.info("replicationResc:{}", replicationResc);
		logger.info("destResc:{}", destResc);
		logger.info("overwrite:{}", overwrite);

		InputStream inputStream;
		try {
			inputStream = file.getInputStream();
		} catch (IOException e) {
			logger.error("Could not get input stream from file: ", e.getMessage());
			throw new DataGridException("Could not get input stream from file.");
		}

		String defaultStorageResource = is.getDefaultStorageResource();
		logger.info("Setting default resource to {}", destResc);

		// Setting temporarily the defaultStorageResource for the logged user
		is.setDefaultStorageResource(destResc);

		boolean isFileUploaded;

		// Getting DataObjectAO in order to create the new file
		IRODSFileFactory irodsFileFactory = is.getIRODSFileFactory();
		Stream2StreamAO stream2StreamA0 = is.getStream2StreamAO();
		IRODSFile targetFile = null;

		try {
			String fileName = file.getOriginalFilename();

			if (fileName.isEmpty())
				fileName = file.getName();

			targetFile = irodsFileFactory.instanceIRODSFile(targetPath, fileName);

			logger.info("targetFile:{}", targetFile);
			// file already exists and we do not want to overwrite it, the transferring is
			// aborted.
			if (targetFile.exists() && !overwrite) {
				String msg = "File already exists. Not overwriting it.";
				logger.info(msg);
				throw new DataGridFileAlreadyExistsException(msg);
			}

			// Transfering file to iRODS filesystem
			stream2StreamA0.transferStreamToFileUsingIOStreams(inputStream, (File) targetFile, 0, BUFFER_SIZE);

			logger.info("transfer complete, compute checksum if required");
			// Computing a check sum for this file just uploaded to iRODS
			if (computeCheckSum)
				fos.computeChecksum(targetPath, fileName);

			// Replicating file into desired resource
			if (replicateFile)
				fos.replicateDataObject(targetFile.getPath(), replicationResc, false);

			if (configService.isUploadRulesEnabled()) {
				logger.info("applying upload rules");
				processUploadRules(targetPath, destResc, targetFile);
			}

			isFileUploaded = true;
		} catch (DataGridFileAlreadyExistsException e) {
			logger.warn("File already exists..will rethrow.");
			throw e;
		} catch (JargonException e) {
			fos.deleteDataObject(targetFile.getPath(), true);
			logger.error("Upload stream failed from Metalnx to the data grid. {}", e.getMessage());
			throw new DataGridException("Upload failed. Resource(s) might be full.");

		} catch (Throwable e) {
			logger.error("Exception in upload processing", e);
			throw new DataGridException("Could not upload due to system exception");
		} finally {
			try {
				inputStream.close(); // Closing streams opened
			} catch (IOException e) {
				logger.error("Could close stream: ", e.getMessage());
			}
		}

		// Setting the default resource back to the original one.
		is.setDefaultStorageResource(defaultStorageResource);

		return isFileUploaded;
	}

	/**
	 * @param targetPath
	 * @param destResc
	 * @param targetFile
	 * @throws DataGridConnectionRefusedException
	 * @throws JargonException
	 * @throws IOException
	 * @throws DataGridRuleException
	 * @throws FileNotFoundException
	 */
	private void processUploadRules(String targetPath, String destResc, IRODSFile targetFile)
			throws DataGridConnectionRefusedException, JargonException, IOException, DataGridRuleException,
			FileNotFoundException {
		// Getting list of resources for upload
		HashMap<String, String> resourceMap = null;
		logger.info("getting resourceMap for upload");

		ResourceAO resourceAO = is.getResourceAO();
		resourceMap = DataGridUtils.buildMapForResourcesNamesAndMountPoints(resourceAO.findAll());
		String objPath = targetFile.getCanonicalPath();
		logger.info("getting file path");
		String filePath = resourceMap.get(destResc) + objPath.substring(objPath.indexOf("/", 1), objPath.length());
		logger.info("file path:{}", filePath);

		logger.info("get resource based on dest:{}", destResc);
		DataGridResource dgDestResc = resourceService.find(destResc);

		if (dgDestResc == null) {
			logger.info("no resource found, ignoring rules");
			/*
			 * this may be further refined in issue File upload when no resource defined can
			 * result in NPE #29 as this functionality is better understood. This gives an
			 * escape route as something of a temporary work-around - mc
			 */
		}

		String host = dgDestResc.getHost();

		logger.info("executing rules...");
		rs.execBamCramMetadataRule(host, objPath, filePath);
		rs.execVCFMetadataRule(host, objPath, filePath);
		rs.execPopulateMetadataRule(host, objPath);
		rs.execImageRule(host, objPath, filePath);
		rs.execIlluminaMetadataRule(dgDestResc, targetPath, objPath);
		rs.execManifestFileRule(host, targetPath, objPath, filePath);
		logger.info("rules executed");
	}

	public ConfigService getConfigService() {
		return configService;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
}
