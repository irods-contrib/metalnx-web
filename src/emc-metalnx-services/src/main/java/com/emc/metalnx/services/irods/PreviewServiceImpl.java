package com.emc.metalnx.services.irods;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.PreviewService;

@Service
@Transactional
public class PreviewServiceImpl implements PreviewService {
	
	@Autowired
	private IRODSServices irodsServices;
	private static final Logger logger = LoggerFactory.getLogger(PreviewServiceImpl.class);

	@Override
	public void filePreview(String path, HttpServletResponse response) {
		// TODO Auto-generated method stub
		logger.info("**************************************PreviewController indexViaUrl() invoked");
		
		logger.info("path ::" + path);
		String fileName = path.substring(path.lastIndexOf("/") + 1, path.length()); 
		logger.info("fileName ::" + fileName);
		
try {
		IRODSFileInputStream irodsFileInputStream = null;
		IRODSFile irodsFile = null; 
		IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();			
		irodsFile = irodsFileFactory.instanceIRODSFile(path);			
		irodsFileInputStream = irodsFileFactory.instanceIRODSFileInputStream(irodsFile);
		
			IOUtils.copy(irodsFileInputStream, response.getOutputStream());
		} catch (IOException | JargonException |DataGridConnectionRefusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 


		
	}

}
