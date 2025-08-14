 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridFilePropertySearch;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

public interface FilePropertyService {

	/**
	 * Get all collections and data objects that match any metadata search criteria
	 * given as a parameters
	 * 
	 * @param searchList
	 *            list of metadata search criteria
	 * @param pageContext
	 *            pagination context for proper counting display at the front end
	 * @param pageNum
	 *            page required
	 * @param pageSize
	 *            max number of items to display in a page
	 * @return list of collections and data objects
	 * @throws DataGridConnectionRefusedException
	 * @throws JargonException
	 */
	public List<DataGridCollectionAndDataObject> findByFileProperties(List<DataGridFilePropertySearch> searchList,
			DataGridPageContext pageContext, int pageNum, int pageSize)
			throws DataGridConnectionRefusedException, JargonException;

}
