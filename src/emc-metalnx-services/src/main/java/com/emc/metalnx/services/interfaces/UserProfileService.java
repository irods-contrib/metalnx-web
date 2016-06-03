/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.services.interfaces;

import java.util.List;

import com.emc.metalnx.core.domain.entity.UserProfile;

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
