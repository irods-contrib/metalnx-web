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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

	private static final String CONTENT_TYPE = "application/octet-stream";


	private static final Logger logger = LoggerFactory.getLogger(PreviewServiceImpl.class);

	@Override
	public byte[] filePreview(String path, HttpServletResponse response) {
		// TODO Auto-generated method stub
		logger.info("getting file preview  for {}", path);

		IRODSFileInputStream irodsFileInputStream = null;
		IRODSFile irodsFile = null; 
		byte[] buffer = null;
		try {

			IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();			
			irodsFile = irodsFileFactory.instanceIRODSFile(path);			
			irodsFileInputStream = irodsFileFactory.instanceIRODSFileInputStream(irodsFile);

			buffer =  IOUtils.toByteArray(irodsFileInputStream);
			
		} catch (IOException | JargonException | DataGridConnectionRefusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		 return buffer;
	}

}
