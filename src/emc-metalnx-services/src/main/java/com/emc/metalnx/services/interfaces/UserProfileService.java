 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.entity.UserProfile;

import java.util.List;

public interface UserProfileService {

    /**
     * Returns the list of all existing profiles on the local DB.
     * 
     * @return
     */
    List<UserProfile> findAll();

    /**
     * Finds a UserProfile object by its id.
     * 
     * @param id
     * @return
     */
    UserProfile findById(Long id);

    /**
     * Returns the list of UserProfiles matching the input string.
     * 
     * @param query
     * @return
     */
    List<UserProfile> findByQueryString(String query);

    /**
     * Creates a new user profile on the DB.
     * 
     * @param userProfile
     */
    Long createUserProfile(UserProfile userProfile);

    /**
     * Updates the UserProfile entity on DB.
     * 
     * @param profile
     * @return
     */
    void modifyUserProfile(UserProfile profile);

    /**
     * Removes a user profile from DB.
     * 
     * @param profile
     */
    void removeUserProfile(UserProfile profile);

    /**
     * Returns the size of the list of all existing profiles on the local DB.
     * 
     * @return
     */
    int countAll();
}
