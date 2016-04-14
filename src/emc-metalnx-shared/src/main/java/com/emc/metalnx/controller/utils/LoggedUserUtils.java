package com.emc.metalnx.controller.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.services.interfaces.UserService;

@Service
public class LoggedUserUtils {

    @Autowired
    UserService userService;

    @Value("${irods.zoneName}")
    private String zoneName;

    public DataGridUser getLoggedDataGridUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) auth.getPrincipal();

        return userService.findByUsernameAndAdditionalInfo(username, zoneName);
    }
}
