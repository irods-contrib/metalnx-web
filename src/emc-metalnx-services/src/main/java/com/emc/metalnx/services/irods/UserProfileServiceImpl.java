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

package com.emc.metalnx.services.irods;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.dao.UserProfileDao;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.UserProfile;
import com.emc.metalnx.services.interfaces.UserProfileService;

@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    UserDao userDao;

    @Autowired
    UserProfileDao userProfileDao;

    @Override
    public List<UserProfile> findAll() {
        return userProfileDao.findAll(UserProfile.class);
    }

    @Override
    public List<UserProfile> findByQueryString(String query) {
        return userProfileDao.findByQueryString(query);
    }

    @Override
    public UserProfile findById(Long id) {
        return userProfileDao.findByID(UserProfile.class, id);
    }

    @Override
    public void modifyUserProfile(UserProfile profile) {
        userProfileDao.merge(profile);
    }

    @Override
    public Long createUserProfile(UserProfile userProfile) {
        return userProfileDao.save(userProfile);
    }

    @Override
    public void removeUserProfile(UserProfile profile) {
        profile = userProfileDao.findByID(UserProfile.class, profile.getProfileId());
        for (DataGridUser user : profile.getUsers()) {
            DataGridUser userToUpdate = userDao.findByID(DataGridUser.class, user.getId());
            userToUpdate.setUserProfile(null);
            userDao.merge(userToUpdate);
        }
        profile.getUsers().clear();
        userProfileDao.delete(profile);
    }

    @Override
    public int countAll() {
        return userProfileDao.findAll(UserProfile.class).size();
    }
}
