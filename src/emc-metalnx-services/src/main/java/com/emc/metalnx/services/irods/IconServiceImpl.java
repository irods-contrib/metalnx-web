package com.emc.metalnx.services.irods;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.services.interfaces.IconService;

@Service
@Transactional
public class IconServiceImpl implements IconService {

	@Override
	public String getIconToDisplayFile(String mimeType) {
	
		String icon = "";

		switch (mimeType) {

		case "application/pdf":
			icon = "fa fa-file-pdf-o fa-4x";
			break;

		case "image/jpg":
		case "image/jpeg":
		case "image/png":
		case "image/gif":
			icon = "fa fa-file-image-o fa-4x";
			break;

		default:
			icon = "fa fa-file fa-4x";
			break;
		}

		return icon;

	}

	@Override
	public String getIconToDisplayCollection() {
		// TODO Auto-generated method stub
		return "fa fa-folder fa-4x";
	}

}
