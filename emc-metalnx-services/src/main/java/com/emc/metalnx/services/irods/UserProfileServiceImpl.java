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
