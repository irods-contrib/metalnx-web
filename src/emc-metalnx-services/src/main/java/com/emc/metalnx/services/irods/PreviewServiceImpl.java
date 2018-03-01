package com.emc.metalnx.services.irods;


import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import com.emc.metalnx.core.domain.entity.enums.DataGridPermType;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.PreviewService;

@Service
@Transactional
public class PreviewServiceImpl implements PreviewService {

	@Autowired
	private IRODSServices irodsServices;

	@Autowired
	CollectionService collectionService;

	private static final Logger logger = LoggerFactory.getLogger(PreviewServiceImpl.class);

	@Override
	public boolean filePreview(String path,HttpServletResponse response) {

		logger.info("getting file preview  for {}", path);

		boolean isCopySuccessFul = true;		
		IRODSFileInputStream irodsFileInputStream = null;
		IRODSFile irodsFile = null; 

		try {
			IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();			
			irodsFile = irodsFileFactory.instanceIRODSFile(path);			
			irodsFileInputStream = irodsFileFactory.instanceIRODSFileInputStream(irodsFile);
			response.setContentType("image/png");												
			FileCopyUtils.copy(irodsFileInputStream, response.getOutputStream());

		} catch (IOException | JargonException | DataGridConnectionRefusedException e) {		
			e.printStackTrace();
			isCopySuccessFul = false;
		} 

		finally {
			try {
				if (irodsFileInputStream != null)
					irodsFileInputStream.close();
				if (irodsFile != null)
					irodsFile.close();
			} catch (Exception e) {
				logger.error("Could not close stream(s): ", e.getMessage());
			}
		}
		return isCopySuccessFul;
	}

	@Override
	public boolean getPermission(String path) {
		boolean permission = true;
		try {
			String permissionType = collectionService.getPermissionsForPath(path);
			if (permissionType.equalsIgnoreCase(DataGridPermType.NONE.name())) {
				permission = false;
			}			
		} catch (DataGridException e) {
			logger.error("Could not download selected items: ", e.getMessage());
		}
		return permission;
	}




}
