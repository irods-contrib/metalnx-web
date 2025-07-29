package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.entity.IconObject;

public interface IconService {

	IconObject getIconToDisplayFile(String mimeType);
	IconObject getIconToDisplayCollection();

}
