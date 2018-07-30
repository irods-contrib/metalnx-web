 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.dao;

import com.emc.metalnx.core.domain.dao.generic.GenericDao;
import com.emc.metalnx.core.domain.entity.UserProfile;

import java.util.List;

public interface UserProfileDao extends GenericDao<UserProfile, Long> {
	
	/**
	 * Returns the list of UserProfiles matching the input string.
	 * @param query
	 * @return
	 */
	List<UserProfile> findByQueryString(String query);

}
