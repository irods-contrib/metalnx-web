package com.emc.metalnx.services.irods;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.entity.IconObject;
import com.emc.metalnx.services.interfaces.IconService;

@Service
@Transactional
public class IconServiceImpl implements IconService {

	private static final Map<String,IconObject> myMap = createMap();
	private static Map<String, IconObject> createMap()
	{
		Map<String,IconObject> myMap = new HashMap<String, IconObject>();
		myMap.put("image/jpg", new IconObject("File" , "fa fa-file-image-o"));
		myMap.put("image/jpeg", new IconObject("File" , "fa fa-file-image-o"));
		myMap.put("image/png", new IconObject("File" , "fa fa-file-image-o"));
		myMap.put("image/gif", new IconObject("File" , "fa fa-file-image-o"));
		myMap.put("application/pdf", new IconObject("File" , "fa fa-file-pdf-o"));
		
		return myMap;
	}


	@Override
	public IconObject getIconToDisplayFile(String mimeType) {
		
		IconObject iconObj = null;
		
		if(myMap.containsKey(mimeType)) 
			iconObj = myMap.get(mimeType);			
		else
			iconObj = new IconObject("File" , "fa fa-file-pdf-o");
		
		return iconObj;

	}

	@Override
	public IconObject getIconToDisplayCollection() {
		// TODO Auto-generated method stub
		return new IconObject("Folder" , "fa fa-folder");
	}



}
