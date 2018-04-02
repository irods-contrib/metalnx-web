package com.emc.metalnx.services.irods;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
	
	private static final Map<String,String> myMap = createMapToGetTemplate();
	private static Map<String, String> createMapToGetTemplate()
	{
		Map<String, String> myMap = new HashMap<String, String>();
		myMap.put("image/jpg", "collections/preview :: imagePreview");
		myMap.put("image/jpeg", "collections/preview :: imagePreview");
		myMap.put("image/png", "collections/preview :: imagePreview");
		myMap.put("image/gif", "collections/preview :: imagePreview");		
		myMap.put("application/pdf", "collections/preview :: pdfFilePreview");		
		myMap.put("text/plain", "collections/preview :: cmFilePreview");	
		myMap.put("application/xml", "collections/preview :: cmFilePreview");	
		myMap.put("application/json", "collections/preview :: cmFilePreview");
		myMap.put("text/html", "collections/preview :: cmFilePreview");
		myMap.put("text/csv", "collections/preview :: csvFilePreview");	
		myMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "collections/preview :: noPreview");
		myMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml.template", "collections/preview :: noPreview");
		myMap.put("application/vnd.ms-excel", "collections/preview :: noPreview");
		myMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "collections/preview :: noPreview");
		myMap.put("application/msword", "collections/preview :: noPreview");
		
		/*myMap.put("application/vnd.ms-excel.template.macroenabled.12", "collections/imagePreview :: filePreview");
		myMap.put("application/vnd.ms-excel.sheet.macroenabled.12", "collections/imagePreview :: filePreview");
*/		
		return myMap;
	}
	

	@Override
	public boolean filePreview(String path, String mimeType, HttpServletResponse response) {

		logger.info("getting file preview  for {} ::"  + path + " and mimetype :: " +mimeType);

		boolean isCopySuccessFul = true;		
		IRODSFileInputStream irodsFileInputStream = null;
		IRODSFile irodsFile = null; 

		try {
			IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();			
			irodsFile = irodsFileFactory.instanceIRODSFile(path);			
			irodsFileInputStream = irodsFileFactory.instanceIRODSFileInputStream(irodsFile);
			response.setContentType(mimeType);												
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

	@Override
	public String getTemplate(String mimeType) {
		String template = "collections/preview :: noPreview";		
		if(myMap.containsKey(mimeType)) 
			template = myMap.get(mimeType);			
		
		return template;
	}


}
