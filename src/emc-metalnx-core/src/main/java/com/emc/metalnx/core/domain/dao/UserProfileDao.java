package com.emc.metalnx.core.domain.dao;

import java.util.List;

import com.emc.metalnx.core.domain.dao.generic.GenericDao;
import com.emc.metalnx.core.domain.entity.UserProfile;

public interface UserProfileDao extends GenericDao<UserProfile, Long> {
	
	/**
	 * Returns the list of UserProfiles matching the input string.
	 * @param query
	 * @return
	 */
	List<UserProfile> findByQueryString(String query);

}
